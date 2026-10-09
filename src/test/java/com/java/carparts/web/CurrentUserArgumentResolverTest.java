package com.java.carparts.web;

import com.java.carparts.security.CurrentUser;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CurrentUserArgumentResolverTest {
    private final CurrentUserArgumentResolver resolver = new CurrentUserArgumentResolver();

    static class Fixture {
        void endpoint(@CurrentUserParam CurrentUser user, String other) { }
    }

    @Test
    void supportsOnlyAnnotatedCurrentUserParameter() throws Exception {
        Method method = Fixture.class.getDeclaredMethod("endpoint", CurrentUser.class, String.class);
        assertThat(resolver.supportsParameter(new MethodParameter(method, 0))).isTrue();
        assertThat(resolver.supportsParameter(new MethodParameter(method, 1))).isFalse();
    }

    @Test
    void resolveArgument_returnsAuthenticatedCurrentUser() throws Exception {
        CurrentUser expected = new CurrentUser("admin", List.of("ADMIN"));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(expected, null, List.of()));
        Method method = Fixture.class.getDeclaredMethod("endpoint", CurrentUser.class, String.class);
        assertThat(resolver.resolveArgument(new MethodParameter(method, 0), null, null, null)).isSameAs(expected);
        SecurityContextHolder.clearContext();
    }
}
