package com.sapient.auth_service.serviceimpl;

import com.sapient.auth_service.modal.User;
import com.sapient.auth_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
 class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userServiceImpl;

    private User user;

    @BeforeEach
     void setUp() {
        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("password");
    }

    @Test
     void testRegisterUser() {
        String encodedPassword = "encodedPassword";
        when(passwordEncoder.encode(user.getPassword())).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(user);

        User registeredUser = userServiceImpl.registerUser(user);

        assertNotNull(registeredUser);
        assertEquals(encodedPassword, registeredUser.getPassword());
//        verify(passwordEncoder, times(1)).encode(user.getPassword());
        verify(userRepository, times(1)).save(user);
    }

    @Test
     void testFindByEmail() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(user);

        User foundUser = userServiceImpl.findByEmail("test@example.com");

        assertNotNull(foundUser);
        assertEquals("test@example.com", foundUser.getEmail());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }
}