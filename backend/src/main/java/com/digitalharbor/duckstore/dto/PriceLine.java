package com.digitalharbor.duckstore.dto;

import java.math.BigDecimal;

/** One itemized adjustment. Negative amount = discount, positive = increment. */
public record PriceLine(String description, BigDecimal amount) {
}
