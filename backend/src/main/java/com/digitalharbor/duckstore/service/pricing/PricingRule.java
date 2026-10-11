package com.digitalharbor.duckstore.service.pricing;

import com.digitalharbor.duckstore.dto.PriceLine;
import java.util.List;


public interface PricingRule {

    List<PriceLine> apply(PricingContext context);
}
