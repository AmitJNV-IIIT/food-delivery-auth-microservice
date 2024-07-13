package com.sapient.auth_service.config;
import com.sapient.auth_service.util.JwtConstant;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;

import static com.mongodb.assertions.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
 class JwtTokenValidatorTest {

    @InjectMocks
    private JwtTokenValidator jwtTokenValidator;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
     void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
     void testValidToken() throws ServletException, IOException {
        String token = Jwts.builder().claim("email", "test@example.com")
                .signWith(Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes())).compact();
        when(request.getHeader(JwtConstant.JWT_HEADER)).thenReturn("Bearer " + token);

        jwtTokenValidator.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
     void testInvalidToken() throws ServletException, IOException {
        when(request.getHeader(JwtConstant.JWT_HEADER)).thenReturn("Bearer invalid_token");

        assertThrows(BadCredentialsException.class, () -> {
            jwtTokenValidator.doFilterInternal(request, response, filterChain);
        });

        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
     void testNoToken() throws ServletException, IOException {
        when(request.getHeader(JwtConstant.JWT_HEADER)).thenReturn(null);

        jwtTokenValidator.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
