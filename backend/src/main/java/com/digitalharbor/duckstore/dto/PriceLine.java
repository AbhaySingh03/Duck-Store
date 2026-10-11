package com.digitalharbor.duckstore.dto;

import java.math.BigDecimal;


public record PriceLine(String description, BigDecimal amount) {
}
