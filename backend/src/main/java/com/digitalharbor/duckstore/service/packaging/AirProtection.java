package com.digitalharbor.duckstore.service.packaging;

import com.digitalharbor.duckstore.entity.enums.PackageType;
import com.digitalharbor.duckstore.entity.enums.Protection;
import com.digitalharbor.duckstore.entity.enums.ShippingMode;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AirProtection implements ProtectionPolicy {
    public ShippingMode mode() {
        return ShippingMode.AIR;
    }

    public List<Protection> protectionFor(PackageType packageType) {
        return packageType == PackageType.PLASTIC
                ? List.of(Protection.BUBBLE_WRAP_BAGS)       
                : List.of(Protection.POLYSTYRENE_BALLS);    
    }
}
