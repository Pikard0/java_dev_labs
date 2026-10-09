package com.java.carparts.validation;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.assertj.core.api.Assertions.assertThat;

class AdminEndpointTest {
    @Test
    void composedAnnotation_containsSecurityAndResponseStatusAnnotations() {
        assertThat(AdminEndpoint.class.isAnnotationPresent(PreAuthorize.class)).isTrue();
        assertThat(AdminEndpoint.class.getAnnotation(ResponseStatus.class).value())
                .isEqualTo(HttpStatus.OK);
    }
}
