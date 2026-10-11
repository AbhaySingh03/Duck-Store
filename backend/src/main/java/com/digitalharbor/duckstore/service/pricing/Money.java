package com.digitalharbor.duckstore.service.pricing;

import java.math.BigDecimal;
import java.math.RoundingMode;


final class Money {

    private Money() {
    }

    static BigDecimal round(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    static BigDecimal percentOf(BigDecimal base, int percent) {
        return round(base.multiply(BigDecimal.valueOf(percent)).movePointLeft(2));
    }
}
