package com.java.carparts.validation;

import com.java.carparts.domain.dto.ProductCreateRequest;
import com.java.carparts.domain.dto.ProductUpdateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Locale;

public class SkuCategoryValidator implements ConstraintValidator<ValidSkuCategory, Object> {
    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) return true;
        String sku;
        String category;
        if (value instanceof ProductCreateRequest request) {
            sku = request.getSku();
            category = request.getCategory();
        } else if (value instanceof ProductUpdateRequest request) {
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
            case "BRK" -> normalized.contains("гальм") || normalized.contains("brake");
            case "OIL" -> normalized.contains("олив") || normalized.contains("oil");
            case "FLT" -> normalized.contains("фільтр") || normalized.contains("filter");
            case "BAT" -> normalized.contains("елект") || normalized.contains("акум") || normalized.contains("battery");
            default -> true;
        };
    }
}
