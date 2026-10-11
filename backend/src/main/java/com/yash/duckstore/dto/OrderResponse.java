package com.yash.duckstore.dto;

import com.yash.duckstore.domain.DuckColor;
import com.yash.duckstore.domain.DuckSize;
import com.yash.duckstore.domain.PackageType;
import com.yash.duckstore.domain.ShippingMode;

import java.math.BigDecimal;
import java.util.List;

public record OrderResponse(
        DuckColor color,
        DuckSize size,
        int quantity,
        String destinationCountry,
        ShippingMode shippingMode,
        BigDecimal unitPrice,
        PackageType packageType,
        List<String> protectionTypes,
        List<PriceLine> priceBreakdown,
        BigDecimal totalToPay
) {
}
