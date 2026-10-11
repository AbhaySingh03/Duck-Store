package com.digitalharbor.duckstore.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** Body for editing a duck: only price and quantity can change. */
public record DuckUpdateRequest(
        @NotNull @DecimalMin(value = "0.01", message = "must be at least 0.01")
        @Digits(integer = 8, fraction = 2, message = "must have at most 2 decimal places") BigDecimal price,
        @NotNull @Min(value = 0, message = "must not be negative") @Max(1_000_000) Integer quantity) {
}
