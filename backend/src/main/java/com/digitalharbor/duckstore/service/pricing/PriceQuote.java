package com.digitalharbor.duckstore.service.pricing;

import com.digitalharbor.duckstore.dto.PriceLine;
import java.math.BigDecimal;
import java.util.List;

public record PriceQuote(BigDecimal subtotal, List<PriceLine> adjustments, BigDecimal total) {
}
