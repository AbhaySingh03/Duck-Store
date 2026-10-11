package com.digitalharbor.duckstore.service.packaging;

import com.digitalharbor.duckstore.entity.enums.PackageType;
import com.digitalharbor.duckstore.entity.enums.Protection;
import com.digitalharbor.duckstore.entity.enums.ShippingMode;
import java.util.List;

/** Strategy: how ducks are protected inside the package for one shipping mode. Add a mode = add a class. */
public interface ProtectionPolicy {

    ShippingMode mode();

    List<Protection> protectionFor(PackageType packageType);
}
