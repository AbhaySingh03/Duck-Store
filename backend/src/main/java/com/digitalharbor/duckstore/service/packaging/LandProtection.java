package com.digitalharbor.duckstore.service.packaging;

import com.digitalharbor.duckstore.entity.enums.PackageType;
import com.digitalharbor.duckstore.entity.enums.Protection;
import com.digitalharbor.duckstore.entity.enums.ShippingMode;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class LandProtection implements ProtectionPolicy {
    public ShippingMode mode() {
        return ShippingMode.LAND;
    }

    public List<Protection> protectionFor(PackageType packageType) {
        return List.of(Protection.POLYSTYRENE_BALLS); 
    }
}
