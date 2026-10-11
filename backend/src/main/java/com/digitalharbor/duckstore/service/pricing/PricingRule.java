package com.digitalharbor.duckstore.service.pricing;

import com.digitalharbor.duckstore.dto.PriceLine;
import java.util.List;

/** Strategy: one pricing concern. Returns the lines it adds (empty when it does not apply). */
public interface PricingRule {

    List<PriceLine> apply(PricingContext context);
}
