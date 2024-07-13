package com.sapient.auth_service.controller;

import com.sapient.auth_service.dto.UserDto;
import com.sapient.auth_service.modal.User;
import com.sapient.auth_service.repository.UserRepository;
import com.sapient.auth_service.dto.LoginRequest;
import com.sapient.auth_service.dto.AuthResponse;
import com.sapient.auth_service.service.CustomerUserDetailsService;
import com.sapient.auth_service.util.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
 class AuthControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private CustomerUserDetailsService customerUserDetailsService;

    @InjectMocks
    private AuthController authController;

    private UserDto userDto;
    private User user;
    private LoginRequest loginRequest;
    private UserDetails userDetails;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        userDto = new UserDto();
        userDto.setEmail("test@example.com");
        userDto.setFullName("Test User");
        userDto.setPassword("password");

        user = new User();
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setPassword("encodedPassword");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        userDetails = org.springframework.security.core.userdetails.User
                .withUsername("test@example.com")
                .password("encodedPassword")
                .authorities("ROLE_USER")
                .build();

        authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Test
     void testCreateUserHandler_Success() {
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(null);
        when(passwordEncoder.encode(userDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(customerUserDetailsService.loadUserByUsername(userDto.getEmail())).thenReturn(userDetails);
        when(passwordEncoder.matches(userDto.getPassword(), "encodedPassword")).thenReturn(true);
        when(jwtProvider.generateToken(any(Authentication.class))).thenReturn("jwtToken");

        ResponseEntity<AuthResponse> response = authController.createUserHandler(userDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isValid());
        assertEquals("Register success", response.getBody().getMessage());

        verify(userRepository, times(1)).findByEmail(userDto.getEmail());
        verify(passwordEncoder, times(1)).encode(userDto.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
        verify(customerUserDetailsService, times(1)).loadUserByUsername(userDto.getEmail());
        verify(jwtProvider, times(1)).generateToken(any(Authentication.class));
    }

    @Test
     void testCreateUserHandler_EmailConflict() {
        when(userRepository.findByEmail(userDto.getEmail())).thenReturn(user);

        ResponseEntity<AuthResponse> response = authController.createUserHandler(userDto);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isValid());
        assertEquals("Email is already used with another account", response.getBody().getMessage());

        verify(userRepository, times(1)).findByEmail(userDto.getEmail());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(customerUserDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtProvider, never()).generateToken(any(Authentication.class));
    }

    @Test
     void testSignin_Success() {
        when(customerUserDetailsService.loadUserByUsername(loginRequest.getEmail())).thenReturn(userDetails);
        when(passwordEncoder.matches(loginRequest.getPassword(), userDetails.getPassword())).thenReturn(true);
        when(jwtProvider.generateToken(any(Authentication.class))).thenReturn("jwtToken");

        ResponseEntity<AuthResponse> response = authController.signin(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isValid());
        assertEquals("Login success", response.getBody().getMessage());

        verify(customerUserDetailsService, times(1)).loadUserByUsername(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), userDetails.getPassword());
        verify(jwtProvider, times(1)).generateToken(any(Authentication.class));
    }

    @Test
     void testSignin_InvalidCredentials() {
        when(customerUserDetailsService.loadUserByUsername(loginRequest.getEmail())).thenReturn(userDetails);
        when(passwordEncoder.matches(loginRequest.getPassword(), userDetails.getPassword())).thenReturn(false);

        assertThrows(BadCredentialsException.class, () -> {
            authController.signin(loginRequest);
        });

        verify(customerUserDetailsService, times(1)).loadUserByUsername(loginRequest.getEmail());
        verify(passwordEncoder, times(1)).matches(loginRequest.getPassword(), userDetails.getPassword());
        verify(jwtProvider, never()).generateToken(any(Authentication.class));
    }

    @Test
     void testValidateToken_Valid() {
        String token = "Bearer jwtToken";
        when(jwtProvider.validateToken("jwtToken")).thenReturn(true);

        ResponseEntity<AuthResponse> response = authController.validateToken(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isValid());
        assertEquals("Token is valid", response.getBody().getMessage());

        verify(jwtProvider, times(1)).validateToken("jwtToken");
    }

    @Test
     void testValidateToken_Invalid() {
        String token = "Bearer invalidToken";
        when(jwtProvider.validateToken("invalidToken")).thenReturn(false);

        ResponseEntity<AuthResponse> response = authController.validateToken(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isValid());
        assertEquals("Token is invalid", response.getBody().getMessage());

        verify(jwtProvider, times(1)).validateToken("invalidToken");
    }

    @Test
     void testValidateToken_MissingBearer() {
        String token = "jwtToken";
        when(jwtProvider.validateToken("jwtToken")).thenReturn(true);

        ResponseEntity<AuthResponse> response = authController.validateToken(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isValid());
        assertEquals("Token is valid", response.getBody().getMessage());

        verify(jwtProvider, times(1)).validateToken("jwtToken");
    }
}
