package com.sapient.auth_service.controller;

import com.sapient.auth_service.dto.UserDto;
import com.sapient.auth_service.util.JwtProvider;
import com.sapient.auth_service.modal.User;
import com.sapient.auth_service.repository.UserRepository;
import com.sapient.auth_service.dto.LoginRequest;
import com.sapient.auth_service.dto.AuthResponse;
import com.sapient.auth_service.service.CustomerUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final String BEARER_PREFIX = "Bearer ";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final CustomerUserDetailsService customerUserDetailsService;

    @Autowired
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtProvider jwtProvider,
                          CustomerUserDetailsService customerUserDetailsService, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.customerUserDetailsService = customerUserDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> createUserHandler(@RequestBody UserDto user) {
        if (userRepository.findByEmail(user.getEmail()) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new AuthResponse(null, "Email is already used with another account", false));
        }

        var createdUser = createUserFromDto(user);
        userRepository.save(createdUser);

        var authentication = authenticate(user.getEmail(), user.getPassword());
        String jwt = jwtProvider.generateToken(authentication);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(jwt, "Register success", true));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> signin(@RequestBody LoginRequest loginRequest) {
        var authentication = authenticate(loginRequest.getEmail(), loginRequest.getPassword());
        String jwt = jwtProvider.generateToken(authentication);

        return ResponseEntity.ok(new AuthResponse(jwt, "Login success", true));
    }

    @GetMapping("/validate-token")
    public ResponseEntity<AuthResponse> validateToken(@RequestHeader("Authorization") String token) {
        if (token.startsWith(BEARER_PREFIX)) {
            token = token.substring(BEARER_PREFIX.length());
        }

        boolean isValid = jwtProvider.validateToken(token);
        String message = isValid ? "Token is valid" : "Token is invalid";
        return ResponseEntity.ok(new AuthResponse(null, message, isValid));
    }

    private User createUserFromDto(UserDto userDto) {
       var user = new User();
        user.setEmail(userDto.getEmail());
        user.setFullName(userDto.getFullName());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        return user;
    }

    Authentication authenticate(String username, String password) {
        var userDetails = customerUserDetailsService.loadUserByUsername(username);

        if (userDetails == null || !passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid username or password.");
        }
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}