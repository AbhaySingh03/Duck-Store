package com.digitalharbor.duckstore.service;

import com.digitalharbor.duckstore.entity.enums.Destination;
import com.digitalharbor.duckstore.entity.enums.PackageType;
import com.digitalharbor.duckstore.entity.enums.ShippingMode;
import com.digitalharbor.duckstore.service.pricing.*;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Pure unit tests for the pricing rules (no Spring, no database). */
class PricingServiceTest {

    private final PricingService pricing = new PricingService(List.of(
            new BulkDiscountRule(), new PackageRule(), new DestinationRule(), new ShippingChargeRule()));

    private PriceQuote quote(int qty, String price, PackageType pkg, Destination dest, ShippingMode mode) {
        return pricing.quote(new PricingContext(qty, new BigDecimal(price), pkg, dest, mode));
    }

    private static BigDecimal amountOf(PriceQuote q, String descriptionStart) {
        return q.adjustments().stream().filter(l -> l.description().startsWith(descriptionStart))
                .findFirst().orElseThrow().amount();
    }

    @Test
    void wood_usa_sea_example() {
        // 2000 + 5% wood (100) + 18% USA (360) + 400 sea
        PriceQuote q = quote(10, "200.00", PackageType.WOOD, Destination.USA, ShippingMode.SEA);
        assertThat(q.subtotal()).isEqualByComparingTo("2000.00");
        assertThat(q.total()).isEqualByComparingTo("2860.00");
    }

    @Test
    void more_than_100_units_gets_20_percent_discount() {
        PriceQuote q = quote(101, "10.00", PackageType.CARDBOARD, Destination.OTHER, ShippingMode.LAND);
        assertThat(amountOf(q, "Bulk discount")).isEqualByComparingTo("-202.00");
        assertThat(amountOf(q, "Cardboard")).isEqualByComparingTo("-10.10");
        assertThat(amountOf(q, "Destination")).isEqualByComparingTo("151.50");
        assertThat(amountOf(q, "Land")).isEqualByComparingTo("1010.00");
        assertThat(q.total()).isEqualByComparingTo("1959.40");
    }

    @Test
    void exactly_100_units_gets_no_discount() {
        PriceQuote q = quote(100, "1.00", PackageType.PLASTIC, Destination.INDIA, ShippingMode.AIR);
        assertThat(q.adjustments()).noneMatch(l -> l.description().startsWith("Bulk"));
        // 100 + 10 plastic + 19 India + 3000 air
        assertThat(q.total()).isEqualByComparingTo("3129.00");
    }

    @Test
    void air_charge_is_not_reduced_at_exactly_1000_units() {
        PriceQuote q = quote(1000, "1.00", PackageType.WOOD, Destination.USA, ShippingMode.AIR);
        assertThat(q.adjustments()).noneMatch(l -> l.description().startsWith("Air bulk"));
        // 1000 - 200 bulk + 50 wood + 180 USA + 30000 air
        assertThat(q.total()).isEqualByComparingTo("31030.00");
    }

    @Test
    void air_charge_is_reduced_15_percent_above_1000_units() {
        PriceQuote q = quote(1001, "1.00", PackageType.CARDBOARD, Destination.BOLIVIA, ShippingMode.AIR);
        assertThat(amountOf(q, "Air shipping")).isEqualByComparingTo("30030.00");
        assertThat(amountOf(q, "Air bulk")).isEqualByComparingTo("-4504.50");
        assertThat(q.total()).isEqualByComparingTo("26446.42");
    }

    @Test
    void amounts_are_rounded_to_the_cent_and_breakdown_adds_up() {
        PriceQuote q = quote(3, "0.33", PackageType.PLASTIC, Destination.INDIA, ShippingMode.SEA);
        assertThat(q.subtotal()).isEqualByComparingTo("0.99");
        assertThat(amountOf(q, "Plastic")).isEqualByComparingTo("0.10");     // 0.099  -> 0.10
        assertThat(amountOf(q, "Destination")).isEqualByComparingTo("0.19"); // 0.1881 -> 0.19
        assertThat(q.total()).isEqualByComparingTo("401.28");
        BigDecimal sum = q.adjustments().stream().map(PriceLine::amount).reduce(q.subtotal(), BigDecimal::add);
        assertThat(sum).isEqualByComparingTo(q.total());
    }

    @Test
    void destination_surcharges() {
        assertThat(Destination.USA.surchargePercent()).isEqualTo(18);
        assertThat(Destination.BOLIVIA.surchargePercent()).isEqualTo(13);
        assertThat(Destination.INDIA.surchargePercent()).isEqualTo(19);
        assertThat(Destination.OTHER.surchargePercent()).isEqualTo(15);
    }

    @Test
    void country_names_are_normalised() {
        assertThat(Destination.from("usa")).isEqualTo(Destination.USA);
        assertThat(Destination.from("  United States ")).isEqualTo(Destination.USA);
        assertThat(Destination.from("BOLIVIA")).isEqualTo(Destination.BOLIVIA);
        assertThat(Destination.from("India")).isEqualTo(Destination.INDIA);
        assertThat(Destination.from("Narnia")).isEqualTo(Destination.OTHER);
    }
}
