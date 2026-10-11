package com.yash.duckstore.service;

import com.yash.duckstore.domain.PackageType;
import com.yash.duckstore.domain.ShippingMode;
import com.yash.duckstore.dto.PriceLine;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PricingServiceTest {

    private final PricingService pricingService = new PricingService();

    @Test
    void appliesNoVolumeDiscountAtExactlyOneHundredUnits() {
        var result = pricingService.calculate(new BigDecimal("10.00"), 100,
                PackageType.CARDBOARD, "India", ShippingMode.SEA);

        assertFalse(result.lines().stream().anyMatch(line -> line.code().equals("VOLUME_DISCOUNT")));
        assertEquals(new BigDecimal("1000.00"), amount(result.lines(), "BASE"));
        assertEquals(new BigDecimal("-10.00"), amount(result.lines(), "CARDBOARD_DISCOUNT"));
        assertEquals(new BigDecimal("190.00"), amount(result.lines(), "DESTINATION_SURCHARGE"));
        assertEquals(new BigDecimal("400.00"), amount(result.lines(), "SEA_SHIPPING"));
        assertEquals(new BigDecimal("1580.00"), result.total());
    }

    @Test
    void appliesTwentyPercentVolumeDiscountAboveOneHundredUnits() {
        var result = pricingService.calculate(new BigDecimal("10.00"), 101,
                PackageType.WOOD, "USA", ShippingMode.LAND);

        assertEquals(new BigDecimal("-202.00"), amount(result.lines(), "VOLUME_DISCOUNT"));
        assertEquals(new BigDecimal("50.50"), amount(result.lines(), "WOOD_PACKAGE"));
        assertEquals(new BigDecimal("181.80"), amount(result.lines(), "DESTINATION_SURCHARGE"));
        assertEquals(new BigDecimal("1010.00"), amount(result.lines(), "LAND_SHIPPING"));
        assertEquals(new BigDecimal("2050.30"), result.total());
    }

    @Test
    void reducesAirShippingByFifteenPercentAboveOneThousandUnits() {
        var result = pricingService.calculate(new BigDecimal("1.00"), 1001,
                PackageType.PLASTIC, "Canada", ShippingMode.AIR);

        assertEquals(new BigDecimal("-4504.50"), amount(result.lines(), "AIR_VOLUME_DISCOUNT"));
        assertEquals(new BigDecimal("25525.50"), amount(result.lines(), "AIR_SHIPPING"));
        assertEquals("OTHER", result.normalizedCountry());
        assertEquals(new BigDecimal("26576.55"), result.total());
    }

    @Test
    void supportsCountryAliasesCaseInsensitively() {
        var us = pricingService.calculate(new BigDecimal("100.00"), 1,
                PackageType.CARDBOARD, " united states ", ShippingMode.SEA);

        assertEquals("USA", us.normalizedCountry());
        assertEquals(new BigDecimal("18.00"), amount(us.lines(), "DESTINATION_SURCHARGE"));
    }

    @Test
    void usesFifteenPercentForOtherDestinations() {
        var result = pricingService.calculate(new BigDecimal("100.00"), 1,
                PackageType.CARDBOARD, "Japan", ShippingMode.SEA);

        assertEquals(new BigDecimal("15.00"), amount(result.lines(), "DESTINATION_SURCHARGE"));
    }

    @Test
    void rejectsNonPositiveUnitPrice() {
        assertThrows(IllegalArgumentException.class, () -> pricingService.calculate(
                new BigDecimal("0.00"), 1, PackageType.WOOD, "India", ShippingMode.LAND));
    }

    @Test
    void rejectsNonPositiveQuantity() {
        assertThrows(IllegalArgumentException.class, () -> pricingService.calculate(
                new BigDecimal("1.00"), 0, PackageType.WOOD, "India", ShippingMode.LAND));
    }

    private BigDecimal amount(java.util.List<PriceLine> lines, String code) {
        return lines.stream()
                .filter(line -> line.code().equals(code))
                .map(PriceLine::amount)
                .reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }
}
