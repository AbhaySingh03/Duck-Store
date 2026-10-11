package com.digitalharbor.duckstore.service.packaging;

import com.digitalharbor.duckstore.entity.enums.PackageType;
import com.digitalharbor.duckstore.entity.enums.Protection;
import com.digitalharbor.duckstore.entity.enums.ShippingMode;
import java.util.List;


public interface ProtectionPolicy {

    ShippingMode mode();

    List<Protection> protectionFor(PackageType packageType);
}
