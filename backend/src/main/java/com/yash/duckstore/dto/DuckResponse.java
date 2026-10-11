package com.yash.duckstore.dto;

import com.yash.duckstore.domain.Duck;
import com.yash.duckstore.domain.DuckColor;
import com.yash.duckstore.domain.DuckSize;

import java.math.BigDecimal;

public record DuckResponse(
        Long id,
        DuckColor color,
        DuckSize size,
        BigDecimal price,
        Integer quantity
) {
    public static DuckResponse from(Duck duck) {
        return new DuckResponse(duck.getId(), duck.getColor(), duck.getSize(),
                duck.getPrice(), duck.getQuantity());
    }
}
