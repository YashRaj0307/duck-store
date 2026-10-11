package com.yash.duckstore.service;

import com.yash.duckstore.domain.Duck;
import com.yash.duckstore.domain.DuckColor;
import com.yash.duckstore.domain.DuckSize;
import com.yash.duckstore.domain.ShippingMode;
import com.yash.duckstore.dto.OrderRequest;
import com.yash.duckstore.repository.DuckRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    DuckRepository duckRepository;

    private final PackagingService packagingService = new PackagingService();
    private final PricingService pricingService = new PricingService();

    @Test
    void pricesOrderUsingCheapestActiveMatchingDuck() {
        Duck cheapest = new Duck(DuckColor.RED, DuckSize.MEDIUM,
                new BigDecimal("10.00"), 50);
        when(duckRepository.findFirstByColorAndSizeAndDeletedFalseOrderByPriceAscIdAsc(
                DuckColor.RED, DuckSize.MEDIUM)).thenReturn(Optional.of(cheapest));

        OrderService service = new OrderService(duckRepository, packagingService, pricingService);
        var response = service.priceOrder(new OrderRequest(DuckColor.RED, DuckSize.MEDIUM,
                2, "India", ShippingMode.LAND));

        assertEquals(new BigDecimal("10.00"), response.unitPrice());
        assertEquals(new BigDecimal("43.60"), response.totalToPay());
        assertEquals("INDIA", response.destinationCountry());
        assertEquals("Cardboard", response.packageType().displayName());
    }
}
