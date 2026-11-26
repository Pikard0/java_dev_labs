package com.java.toystore.validation;

import com.java.toystore.domain.dto.ToyCreateRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ToyCategoryValidatorTest {
    private final ToyCategoryValidator validator = new ToyCategoryValidator();

    @Test
    void matchingSkuAndCategory_isValid() {
        var request = new ToyCreateRequest("BLK-001", "Конструктор", "Конструктори", "LEGO", "6+", BigDecimal.ONE, 1);
        assertThat(validator.isValid(request, null)).isTrue();
    }

    @Test
    void mismatchedSkuAndCategory_isInvalid() {
        var request = new ToyCreateRequest("BLK-001", "Конструктор", "Пазли", "LEGO", "6+", BigDecimal.ONE, 1);
        assertThat(validator.isValid(request, null)).isFalse();
    }
}
