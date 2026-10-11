package com.yash.duckstore.dto;

import com.yash.duckstore.domain.DuckColor;
import com.yash.duckstore.domain.DuckSize;
import com.yash.duckstore.domain.ShippingMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record OrderRequest(
        @NotNull(message = "Color is required")
        DuckColor color,

        @NotNull(message = "Size is required")
        DuckSize size,

        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero")
        Integer quantity,

        @NotBlank(message = "Destination country is required")
        @Size(max = 100, message = "Destination country must be at most 100 characters")
        String destinationCountry,

        @NotNull(message = "Shipping mode is required")
        ShippingMode shippingMode
) {
}
