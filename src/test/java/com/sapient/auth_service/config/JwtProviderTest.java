package com.sapient.auth_service.config;

import com.sapient.auth_service.util.JwtConstant;
import com.sapient.auth_service.util.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import javax.crypto.SecretKey;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
 class JwtProviderTest {

    @Mock
    private Authentication authentication;

    private JwtProvider jwtProvider;
    private SecretKey key;

    @BeforeEach
     void setUp() {
        jwtProvider = new JwtProvider();
        key = Keys.hmacShaKeyFor(JwtConstant.SECRET_KEY.getBytes());
    }

    @Test
     void testGenerateToken() {
        when(authentication.getName()).thenReturn("test@example.com");

        String token = jwtProvider.generateToken(authentication);
        assertNotNull(token);

        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        assertEquals("test@example.com", claims.getSubject());
        assertTrue(claims.getExpiration().after(new Date()));
    }

    @Test
     void testValidateToken_ValidToken() {
        when(authentication.getName()).thenReturn("test@example.com");
        String token = jwtProvider.generateToken(authentication);

        assertTrue(jwtProvider.validateToken(token));
    }

    @Test
     void testValidateToken_InvalidToken() {
        String token = "invalid_token";

        assertFalse(jwtProvider.validateToken(token));
    }

    @Test
     void testGetEmailFromJwtToken() {
        String token = Jwts.builder().claim("email", "test@example.com")
                .signWith(key).compact();
        String prefixedToken = "Bearer " + token;

        String email = jwtProvider.getEmailFromJwtToken(prefixedToken);
        assertEquals("test@example.com", email);
    }
}
