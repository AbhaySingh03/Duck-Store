package com.digitalharbor.duckstore.service.pricing;

import com.digitalharbor.duckstore.dto.PriceLine;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;


@Service
public class PricingService {

    private final List<PricingRule> rules;

    public PricingService(List<PricingRule> rules) {
        this.rules = rules;
    }

    public PriceQuote quote(PricingContext context) {
        BigDecimal subtotal = context.subtotal();
        List<PriceLine> adjustments = rules.stream().flatMap(rule -> rule.apply(context).stream()).toList();
        BigDecimal total = adjustments.stream().map(PriceLine::amount).reduce(subtotal, BigDecimal::add);
        return new PriceQuote(subtotal, adjustments, total);
    }
}
