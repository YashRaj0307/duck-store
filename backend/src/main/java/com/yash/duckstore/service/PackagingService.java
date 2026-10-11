package com.yash.duckstore.service;

import com.yash.duckstore.domain.DuckSize;
import com.yash.duckstore.domain.PackageType;
import com.yash.duckstore.domain.ShippingMode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PackagingService {

    public PackageType packageFor(DuckSize size) {
        return switch (size) {
            case XLARGE, LARGE -> PackageType.WOOD;
            case MEDIUM -> PackageType.CARDBOARD;
            case SMALL, XSMALL -> PackageType.PLASTIC;
        };
    }

    public List<String> protectionFor(PackageType packageType, ShippingMode shippingMode) {
        return switch (shippingMode) {
            case LAND -> List.of("Polystyrene balls");
            case SEA -> List.of("Moisture-absorbing beads", "Bubble-wrap bags");
            case AIR -> packageType == PackageType.PLASTIC
                    ? List.of("Bubble-wrap bags")
                    : List.of("Polystyrene balls");
        };
    }
}
