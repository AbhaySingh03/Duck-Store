package com.digitalharbor.duckstore.dto;

import com.digitalharbor.duckstore.entity.enums.Color;
import com.digitalharbor.duckstore.entity.enums.ShippingMode;
import com.digitalharbor.duckstore.entity.enums.Size;
import jakarta.validation.constraints.*;

public record OrderRequest(
        @NotNull Color color,
        @NotNull Size size,
        @NotNull @Min(value = 1, message = "must be at least 1") @Max(1_000_000) Integer quantity,
        @NotBlank(message = "must not be blank") String country,
        @NotNull ShippingMode shippingMode) {
}
