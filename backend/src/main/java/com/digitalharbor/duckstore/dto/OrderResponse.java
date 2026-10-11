package com.digitalharbor.duckstore.dto;

import com.digitalharbor.duckstore.entity.enums.PackageType;
import com.digitalharbor.duckstore.entity.enums.Protection;
import java.math.BigDecimal;
import java.util.List;

/** What the client gets back: packaging, protection, price and an itemized breakdown. */
public record OrderResponse(
        PackageType packageType,
        List<Protection> protections,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal,
        List<PriceLine> breakdown,
        BigDecimal totalToPay) {
}
