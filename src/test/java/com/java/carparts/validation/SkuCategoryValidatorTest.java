package com.java.carparts.validation;

import com.java.carparts.domain.dto.ProductCreateRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SkuCategoryValidatorTest {
    private final SkuCategoryValidator validator = new SkuCategoryValidator();

    @Test
    void matchingSkuAndCategory_isValid() {
        var request = new ProductCreateRequest("BRK-001", "Колодки", "Гальмівна система", "Brembo", null, BigDecimal.ONE, 1);
        assertThat(validator.isValid(request, null)).isTrue();
    }

    @Test
    void mismatchedSkuAndCategory_isInvalid() {
        var request = new ProductCreateRequest("BRK-001", "Колодки", "Фільтри", "Brembo", null, BigDecimal.ONE, 1);
        assertThat(validator.isValid(request, null)).isFalse();
    }
}
