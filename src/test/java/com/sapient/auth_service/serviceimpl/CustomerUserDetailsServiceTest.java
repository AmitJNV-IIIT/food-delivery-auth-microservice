package com.sapient.auth_service.serviceimpl;

import com.sapient.auth_service.modal.User;
import com.sapient.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomerUserDetailsServiceImpl customerUserDetailsService;

    private User user;

    @BeforeEach
     void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(String.valueOf(1L));
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFullName("Test User");
    }

    @Test
     void testLoadUserByUsername_UserExists() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);

        // Act
        UserDetails userDetails = customerUserDetailsService.loadUserByUsername("test@example.com");

        // Assert
        assertNotNull(userDetails);
        assertEquals(user.getEmail(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        assertTrue(userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("USER")));
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
     void testLoadUserByUsername_UserNotFound() {
        // Arrange
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(null);

        // Act & Assert
        assertThrows(UsernameNotFoundException.class, () -> {
            customerUserDetailsService.loadUserByUsername("unknown@example.com");
        });
        verify(userRepository, times(1)).findByEmail("unknown@example.com");
    }
}
