package com.digitalharbor.duckstore.service.pricing;

import com.digitalharbor.duckstore.entity.enums.Destination;
import com.digitalharbor.duckstore.entity.enums.PackageType;
import com.digitalharbor.duckstore.entity.enums.ShippingMode;
import java.math.BigDecimal;

/** Everything a pricing rule may look at. */
public record PricingContext(int quantity, BigDecimal unitPrice, PackageType packageType,
                             Destination destination, ShippingMode shippingMode) {

    /** Rule 1: quantity x price. All percentage rules are calculated on this amount. */
    public BigDecimal subtotal() {
        return Money.round(unitPrice.multiply(BigDecimal.valueOf(quantity)));
    }
}
