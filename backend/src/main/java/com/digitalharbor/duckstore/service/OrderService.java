package com.digitalharbor.duckstore.service;

import com.digitalharbor.duckstore.dto.OrderRequest;
import com.digitalharbor.duckstore.dto.OrderResponse;
import com.digitalharbor.duckstore.entity.enums.Destination;
import com.digitalharbor.duckstore.service.packaging.PackagingResult;
import com.digitalharbor.duckstore.service.packaging.PackagingService;
import com.digitalharbor.duckstore.service.pricing.PriceQuote;
import com.digitalharbor.duckstore.service.pricing.PricingContext;
import com.digitalharbor.duckstore.service.pricing.PricingService;
import java.math.BigDecimal;
import org.springframework.stereotype.Service;


@Service
public class OrderService {

    private final DuckService warehouse;
    private final PackagingService packaging;
    private final PricingService pricing;

    public OrderService(DuckService warehouse, PackagingService packaging, PricingService pricing) {
        this.warehouse = warehouse;
        this.packaging = packaging;
        this.pricing = pricing;
    }

    public OrderResponse place(OrderRequest order) {
        BigDecimal unitPrice = warehouse.currentPrice(order.color(), order.size());
        PackagingResult pack = packaging.pack(order.size(), order.shippingMode());

        PricingContext context = new PricingContext(order.quantity(), unitPrice, pack.packageType(),
                Destination.from(order.country()), order.shippingMode());
        PriceQuote quote = pricing.quote(context);

        return new OrderResponse(pack.packageType(), pack.protections(), unitPrice, order.quantity(),
                quote.subtotal(), quote.adjustments(), quote.total());
    }
}
