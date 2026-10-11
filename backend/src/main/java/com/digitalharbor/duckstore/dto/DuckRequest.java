package com.digitalharbor.duckstore.dto;

import com.digitalharbor.duckstore.entity.enums.Color;
import com.digitalharbor.duckstore.entity.enums.Size;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** Body for adding a duck. */
public record DuckRequest(
        @NotNull Color color,
        @NotNull Size size,
        @NotNull @DecimalMin(value = "0.01", message = "must be at least 0.01")
        @Digits(integer = 8, fraction = 2, message = "must have at most 2 decimal places") BigDecimal price,
        @NotNull @Min(value = 1, message = "must be at least 1") @Max(1_000_000) Integer quantity) {
}
