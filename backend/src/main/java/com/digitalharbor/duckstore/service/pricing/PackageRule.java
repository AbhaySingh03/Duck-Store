package com.digitalharbor.duckstore.service.pricing;

import com.digitalharbor.duckstore.dto.PriceLine;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Rules 3-5: wood +5%, plastic +10%, cardboard -1%. */
@Component
@Order(2)
public class PackageRule implements PricingRule {

    @Override
    public List<PriceLine> apply(PricingContext c) {
        return switch (c.packageType()) {
            case WOOD -> List.of(line("Wood package (+5%)", c, 5));
            case PLASTIC -> List.of(line("Plastic package (+10%)", c, 10));
            case CARDBOARD -> List.of(line("Cardboard package discount (-1%)", c, -1));
        };
    }

    private static PriceLine line(String description, PricingContext c, int percent) {
        return new PriceLine(description, Money.percentOf(c.subtotal(), percent));
    }
}
