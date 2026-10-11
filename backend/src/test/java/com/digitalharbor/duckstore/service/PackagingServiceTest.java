package com.digitalharbor.duckstore.service;

import com.digitalharbor.duckstore.entity.enums.PackageType;
import com.digitalharbor.duckstore.entity.enums.Protection;
import com.digitalharbor.duckstore.entity.enums.ShippingMode;
import com.digitalharbor.duckstore.entity.enums.Size;
import com.digitalharbor.duckstore.service.packaging.*;
import java.util.List;
import org.junit.jupiter.api.Test;

import static com.digitalharbor.duckstore.entity.enums.PackageType.*;
import static com.digitalharbor.duckstore.entity.enums.Protection.*;
import static com.digitalharbor.duckstore.entity.enums.ShippingMode.*;
import static com.digitalharbor.duckstore.entity.enums.Size.*;
import static org.assertj.core.api.Assertions.assertThat;

class PackagingServiceTest {

    private final PackagingService packaging = new PackagingService(
            List.of(new LandProtection(), new AirProtection(), new SeaProtection()));

    private void expect(Size size, ShippingMode mode, PackageType type, Protection... protections) {
        PackagingResult r = packaging.pack(size, mode);
        assertThat(r.packageType()).as("package for %s", size).isEqualTo(type);
        assertThat(r.protections()).as("protection for %s by %s", size, mode).containsExactly(protections);
    }

    @Test
    void package_depends_on_size() {
        assertThat(packaging.pack(XLARGE, LAND).packageType()).isEqualTo(WOOD);
        assertThat(packaging.pack(LARGE, LAND).packageType()).isEqualTo(WOOD);
        assertThat(packaging.pack(MEDIUM, LAND).packageType()).isEqualTo(CARDBOARD);
        assertThat(packaging.pack(SMALL, LAND).packageType()).isEqualTo(PLASTIC);
        assertThat(packaging.pack(XSMALL, LAND).packageType()).isEqualTo(PLASTIC);
    }

    @Test
    void air_wood_or_cardboard_uses_polystyrene_balls() {
        expect(XLARGE, AIR, WOOD, POLYSTYRENE_BALLS);
        expect(MEDIUM, AIR, CARDBOARD, POLYSTYRENE_BALLS);
    }

    @Test
    void air_plastic_uses_bubble_wrap() {
        expect(SMALL, AIR, PLASTIC, BUBBLE_WRAP_BAGS);
        expect(XSMALL, AIR, PLASTIC, BUBBLE_WRAP_BAGS);
    }

    @Test
    void land_always_uses_polystyrene_balls() {
        expect(LARGE, LAND, WOOD, POLYSTYRENE_BALLS);
        expect(MEDIUM, LAND, CARDBOARD, POLYSTYRENE_BALLS);
        expect(SMALL, LAND, PLASTIC, POLYSTYRENE_BALLS);
    }

    @Test
    void sea_always_uses_beads_and_bubble_wrap() {
        expect(XLARGE, SEA, WOOD, MOISTURE_ABSORBING_BEADS, BUBBLE_WRAP_BAGS);
        expect(MEDIUM, SEA, CARDBOARD, MOISTURE_ABSORBING_BEADS, BUBBLE_WRAP_BAGS);
        expect(XSMALL, SEA, PLASTIC, MOISTURE_ABSORBING_BEADS, BUBBLE_WRAP_BAGS);
    }
}
