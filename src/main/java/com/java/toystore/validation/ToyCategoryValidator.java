package com.java.toystore.validation;

import com.java.toystore.domain.dto.ToyCreateRequest;
import com.java.toystore.domain.dto.ToyUpdateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Locale;

public class ToyCategoryValidator implements ConstraintValidator<ValidToyCategory, Object> {
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return true;
        String sku;
        String category;
        if (value instanceof ToyCreateRequest request) {
            sku = request.getSku();
            category = request.getCategory();
        } else if (value instanceof ToyUpdateRequest request) {
            // SKU is immutable during update, so only the category's own validity is checked.
            category = request.getCategory();
            return category != null && !category.isBlank();
        } else {
            return true;
        }
        if (sku == null || sku.isBlank() || category == null || category.isBlank()) return true;
        String prefix = sku.trim().toUpperCase(Locale.ROOT).split("-")[0];
        String normalized = category.trim().toLowerCase(Locale.ROOT);
        return switch (prefix) {
            case "BLK" -> normalized.contains("конструкт") || normalized.contains("block");
            case "DOL" -> normalized.contains("ляль") || normalized.contains("doll");
            case "PUZ" -> normalized.contains("пазл") || normalized.contains("puzzle");
            case "PLU" -> normalized.contains("м'як") || normalized.contains("плюш") || normalized.contains("plush");
            default -> true;
        };
    }
}
