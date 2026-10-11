package com.digitalharbor.duckstore.service.packaging;

import com.digitalharbor.duckstore.entity.enums.PackageType;
import com.digitalharbor.duckstore.entity.enums.Protection;
import java.util.List;

public record PackagingResult(PackageType packageType, List<Protection> protections) {
}
