package com.tecmilenio.mapsconect.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtAuthenticationFilterTest {
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test void disabledAccountCannotReuseToken() throws Exception { check(true); }
    @Test void activeAccountCanUseToken() throws Exception { check(false); }

    private void check(boolean disabled) throws Exception {
        var provider = mock(JwtTokenProvider.class);
        var users = mock(UserDetailsService.class);
        when(provider.validateToken("test-token")).thenReturn(true);
        when(provider.getUsernameFromToken("test-token")).thenReturn("test@tecmilenio.mx");
        when(users.loadUserByUsername("test@tecmilenio.mx")).thenReturn(
            User.withUsername("test@tecmilenio.mx").password("unused")
                .roles("ESTUDIANTE").disabled(disabled).build());
        var filter = new JwtAuthenticationFilter();
        ReflectionTestUtils.setField(filter, "tokenProvider", provider);
        ReflectionTestUtils.setField(filter, "userDetailsService", users);
        var request = new MockHttpServletRequest("GET", "/api/auth/perfil-estado");
        request.addHeader("Authorization", "Bearer test-token");
        filter.doFilter(request, new MockHttpServletResponse(), (req, res) -> {
            assertEquals(!disabled, SecurityContextHolder.getContext().getAuthentication() != null);
        });
    }
}
