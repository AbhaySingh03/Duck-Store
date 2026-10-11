package com.digitalharbor.duckstore.service.packaging;

import com.digitalharbor.duckstore.entity.enums.PackageType;
import com.digitalharbor.duckstore.entity.enums.Protection;
import com.digitalharbor.duckstore.entity.enums.ShippingMode;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SeaProtection implements ProtectionPolicy {
    public ShippingMode mode() {
        return ShippingMode.SEA;
    }

    public List<Protection> protectionFor(PackageType packageType) {
        return List.of(Protection.MOISTURE_ABSORBING_BEADS, Protection.BUBBLE_WRAP_BAGS); // rule 7: any package
    }
}
