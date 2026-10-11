package com.digitalharbor.duckstore.service.pricing;

import com.digitalharbor.duckstore.dto.PriceLine;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Component
@Order(1)
public class BulkDiscountRule implements PricingRule {

    static final int THRESHOLD = 100;
    static final int PERCENT = 20;

    @Override
    public List<PriceLine> apply(PricingContext c) {
        if (c.quantity() <= THRESHOLD) {
            return List.of();
        }
        return List.of(new PriceLine("Bulk discount (" + PERCENT + "% for more than " + THRESHOLD + " units)",
                Money.percentOf(c.subtotal(), PERCENT).negate()));
    }
}
