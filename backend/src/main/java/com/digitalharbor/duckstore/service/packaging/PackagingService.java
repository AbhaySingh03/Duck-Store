 package com.digitalharbor.duckstore.service.packaging;

import com.digitalharbor.duckstore.entity.enums.PackageType;
import com.digitalharbor.duckstore.entity.enums.ShippingMode;
import com.digitalharbor.duckstore.entity.enums.Size;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Decides the package from the duck size (rules 1-3) and the protection from the shipping mode (rules 4-7). */
@Service
public class PackagingService {

    private final Map<ShippingMode, ProtectionPolicy> policies = new EnumMap<>(ShippingMode.class);

    public PackagingService(List<ProtectionPolicy> protectionPolicies) {
        protectionPolicies.forEach(p -> policies.put(p.mode(), p));
        for (ShippingMode mode : ShippingMode.values()) {
            if (!policies.containsKey(mode)) {
                throw new IllegalStateException("No ProtectionPolicy registered for " + mode);
            }
        }
    }

    public PackagingResult pack(Size size, ShippingMode mode) {
        PackageType packageType = packageFor(size);
        return new PackagingResult(packageType, policies.get(mode).protectionFor(packageType));
    }

    private static PackageType packageFor(Size size) {
        return switch (size) {
            case XLARGE, LARGE -> PackageType.WOOD;
            case MEDIUM -> PackageType.CARDBOARD;
            case SMALL, XSMALL -> PackageType.PLASTIC;
        };
    }
}
