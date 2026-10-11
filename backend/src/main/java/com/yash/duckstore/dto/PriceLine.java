package com.yash.duckstore.dto;

import java.math.BigDecimal;

public record PriceLine(
        String code,
        String description,
        BigDecimal amount
) {
}
