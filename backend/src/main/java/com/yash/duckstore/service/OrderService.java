package com.yash.duckstore.service;

import com.yash.duckstore.domain.Duck;
import com.yash.duckstore.domain.PackageType;
import com.yash.duckstore.dto.OrderRequest;
import com.yash.duckstore.dto.OrderResponse;
import com.yash.duckstore.exception.ResourceNotFoundException;
import com.yash.duckstore.repository.DuckRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private final DuckRepository duckRepository;
    private final PackagingService packagingService;
    private final PricingService pricingService;

    public OrderService(DuckRepository duckRepository,
                        PackagingService packagingService,
                        PricingService pricingService) {
        this.duckRepository = duckRepository;
        this.packagingService = packagingService;
        this.pricingService = pricingService;
    }

    @Transactional(readOnly = true)
    public OrderResponse priceOrder(OrderRequest request) {
        // The request has no price. To resolve multiple matching price points deterministically,
        // the assignment policy is to use the lowest active unit price for the requested color and size.
        Duck duck = duckRepository.findFirstByColorAndSizeAndDeletedFalseOrderByPriceAscIdAsc(
                        request.color(), request.size())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active duck exists for color " + request.color().displayName()
                                + " and size " + request.size().displayName()));

        PackageType packageType = packagingService.packageFor(request.size());
        var protections = packagingService.protectionFor(packageType, request.shippingMode());
        var pricing = pricingService.calculate(duck.getPrice(), request.quantity(), packageType,
                request.destinationCountry(), request.shippingMode());

        return new OrderResponse(request.color(), request.size(), request.quantity(),
                pricing.normalizedCountry(), request.shippingMode(), duck.getPrice(),
                packageType, protections, pricing.lines(), pricing.total());
    }
}
