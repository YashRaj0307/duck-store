package com.yash.duckstore.service;

import com.yash.duckstore.domain.DuckSize;
import com.yash.duckstore.domain.PackageType;
import com.yash.duckstore.domain.ShippingMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PackagingServiceTest {

    private final PackagingService packagingService = new PackagingService();

    @Test
    void mapsEverySizeToRequiredPackage() {
        assertEquals(PackageType.WOOD, packagingService.packageFor(DuckSize.XLARGE));
        assertEquals(PackageType.WOOD, packagingService.packageFor(DuckSize.LARGE));
        assertEquals(PackageType.CARDBOARD, packagingService.packageFor(DuckSize.MEDIUM));
        assertEquals(PackageType.PLASTIC, packagingService.packageFor(DuckSize.SMALL));
        assertEquals(PackageType.PLASTIC, packagingService.packageFor(DuckSize.XSMALL));
    }

    @Test
    void selectsAirProtectionBasedOnPackage() {
        assertEquals(java.util.List.of("Polystyrene balls"),
                packagingService.protectionFor(PackageType.WOOD, ShippingMode.AIR));
        assertEquals(java.util.List.of("Polystyrene balls"),
                packagingService.protectionFor(PackageType.CARDBOARD, ShippingMode.AIR));
        assertEquals(java.util.List.of("Bubble-wrap bags"),
                packagingService.protectionFor(PackageType.PLASTIC, ShippingMode.AIR));
    }

    @Test
    void seaShipmentUsesBothRequiredProtectionTypes() {
        assertEquals(java.util.List.of("Moisture-absorbing beads", "Bubble-wrap bags"),
                packagingService.protectionFor(PackageType.WOOD, ShippingMode.SEA));
    }

    @Test
    void landShipmentAlwaysUsesPolystyreneBalls() {
        for (PackageType type : PackageType.values()) {
            assertEquals(java.util.List.of("Polystyrene balls"),
                    packagingService.protectionFor(type, ShippingMode.LAND));
        }
    }
}
