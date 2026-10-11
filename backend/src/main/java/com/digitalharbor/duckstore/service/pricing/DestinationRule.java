package com.digitalharbor.duckstore.service.pricing;

import com.digitalharbor.duckstore.dto.PriceLine;
import com.digitalharbor.duckstore.entity.enums.Destination;
import java.util.List;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Rules 6-9: USA +18%, Bolivia +13%, India +19%, anywhere else +15%. */
@Component
@Order(3)
public class DestinationRule implements PricingRule {

    @Override
    public List<PriceLine> apply(PricingContext c) {
        int percent = c.destination().surchargePercent();
        String where = switch (c.destination()) {
            case USA -> "USA";
            case BOLIVIA -> "Bolivia";
            case INDIA -> "India";
            case OTHER -> "other countries";
        };
        return List.of(new PriceLine("Destination surcharge: " + where + " (+" + percent + "%)",
                Money.percentOf(c.subtotal(), percent)));
    }
}
