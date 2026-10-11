package com.digitalharbor.duckstore.service.pricing;

import com.digitalharbor.duckstore.dto.PriceLine;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Rules 10-12: sea +400 flat, land +10 per unit, air +30 per unit (-15% of the air charge above 1000 units). */
@Component
@Order(4)
public class ShippingChargeRule implements PricingRule {

    static final BigDecimal SEA_FLAT = new BigDecimal("400.00");
    static final BigDecimal LAND_PER_UNIT = new BigDecimal("10");
    static final BigDecimal AIR_PER_UNIT = new BigDecimal("30");
    static final int AIR_BULK_THRESHOLD = 1000;
    static final int AIR_BULK_REDUCTION_PERCENT = 15;

    @Override
    public List<PriceLine> apply(PricingContext c) {
        return switch (c.shippingMode()) {
            case SEA -> List.of(new PriceLine("Sea shipping (flat)", SEA_FLAT));
            case LAND -> List.of(new PriceLine("Land shipping (10 USD per unit)", perUnit(LAND_PER_UNIT, c)));
            case AIR -> air(c);
        };
    }

    private List<PriceLine> air(PricingContext c) {
        BigDecimal charge = perUnit(AIR_PER_UNIT, c);
        PriceLine airLine = new PriceLine("Air shipping (30 USD per unit)", charge);
        if (c.quantity() <= AIR_BULK_THRESHOLD) {
            return List.of(airLine);
        }
        return List.of(airLine, new PriceLine(
                "Air bulk reduction (-" + AIR_BULK_REDUCTION_PERCENT + "% of air charge for more than "
                        + AIR_BULK_THRESHOLD + " units)",
                Money.percentOf(charge, AIR_BULK_REDUCTION_PERCENT).negate()));
    }

    private static BigDecimal perUnit(BigDecimal rate, PricingContext c) {
        return Money.round(rate.multiply(BigDecimal.valueOf(c.quantity())));
    }
}
