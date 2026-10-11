package com.yash.duckstore.service;

import com.yash.duckstore.domain.PackageType;
import com.yash.duckstore.domain.ShippingMode;
import com.yash.duckstore.dto.PriceLine;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class PricingService {

    private static final BigDecimal ZERO = new BigDecimal("0.00");

    public PricingResult calculate(BigDecimal unitPrice, int quantity, PackageType packageType,
                                   String destinationCountry, ShippingMode shippingMode) {
        if (unitPrice == null || unitPrice.signum() <= 0) {
            throw new IllegalArgumentException("Unit price must be greater than zero");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        BigDecimal base = money(unitPrice.multiply(BigDecimal.valueOf(quantity)));
        List<PriceLine> lines = new ArrayList<>();
        lines.add(new PriceLine("BASE", "Unit price × quantity", base));

        if (quantity > 100) {
            addPercent(lines, "VOLUME_DISCOUNT", "20% volume discount", base,
                    new BigDecimal("-0.20"));
        }

        switch (packageType) {
            case WOOD -> addPercent(lines, "WOOD_PACKAGE", "Wood package surcharge (5% of base)", base,
                    new BigDecimal("0.05"));
            case PLASTIC -> addPercent(lines, "PLASTIC_PACKAGE", "Plastic package surcharge (10% of base)", base,
                    new BigDecimal("0.10"));
            case CARDBOARD -> addPercent(lines, "CARDBOARD_DISCOUNT", "Cardboard package discount (1% of base)", base,
                    new BigDecimal("-0.01"));
        }

        String country = normalizeCountry(destinationCountry);
        BigDecimal countryRate = countryRate(country);
        addPercent(lines, "DESTINATION_SURCHARGE",
                country + " destination surcharge (" + countryRate.multiply(new BigDecimal("100"))
                        .stripTrailingZeros().toPlainString() + "% of base)",
                base, countryRate);

        switch (shippingMode) {
            case SEA -> lines.add(new PriceLine("SEA_SHIPPING",
                    "Sea shipping flat charge", money(new BigDecimal("400.00"))));
            case LAND -> lines.add(new PriceLine("LAND_SHIPPING",
                    "Land shipping (10 USD × quantity)", money(new BigDecimal("10.00")
                    .multiply(BigDecimal.valueOf(quantity)))));
            case AIR -> {
                BigDecimal airCharge = new BigDecimal("30.00").multiply(BigDecimal.valueOf(quantity));
                if (quantity > 1000) {
                    airCharge = airCharge.multiply(new BigDecimal("0.85"));
                    lines.add(new PriceLine("AIR_VOLUME_DISCOUNT",
                            "15% reduction on air shipping for orders over 1000 units",
                            money(airCharge.subtract(new BigDecimal("30.00")
                                    .multiply(BigDecimal.valueOf(quantity))).setScale(2, RoundingMode.HALF_UP))));
                } else {
                    lines.add(new PriceLine("AIR_SHIPPING",
                            "Air shipping (30 USD × quantity)", money(airCharge)));
                }
                if (quantity > 1000) {
                    lines.add(new PriceLine("AIR_SHIPPING",
                            "Air shipping after 15% volume reduction", money(airCharge)));
                }
            }
        }

        BigDecimal total = lines.stream()
                .map(PriceLine::amount)
                .reduce(ZERO, BigDecimal::add);
        return new PricingResult(List.copyOf(lines), money(total), country);
    }

    private void addPercent(List<PriceLine> lines, String code, String description,
                            BigDecimal base, BigDecimal rate) {
        BigDecimal amount = money(base.multiply(rate));
        if (amount.signum() != 0) {
            lines.add(new PriceLine(code, description, amount));
        }
    }

    private BigDecimal countryRate(String country) {
        return switch (country) {
            case "USA" -> new BigDecimal("0.18");
            case "BOLIVIA" -> new BigDecimal("0.13");
            case "INDIA" -> new BigDecimal("0.19");
            default -> new BigDecimal("0.15");
        };
    }

    private String normalizeCountry(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Destination country is required");
        }
        String country = input.trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", " ");
        return switch (country) {
            case "US", "USA", "UNITED STATES", "UNITED STATES OF AMERICA" -> "USA";
            case "BO", "BOLIVIA", "PLURINATIONAL STATE OF BOLIVIA" -> "BOLIVIA";
            case "IN", "INDIA", "REPUBLIC OF INDIA" -> "INDIA";
            default -> "OTHER";
        };
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public record PricingResult(List<PriceLine> lines, BigDecimal total, String normalizedCountry) {
    }
}
