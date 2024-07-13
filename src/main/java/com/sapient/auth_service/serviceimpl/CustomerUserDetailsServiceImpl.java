package com.sapient.auth_service.serviceimpl;
import com.sapient.auth_service.repository.UserRepository;
import com.sapient.auth_service.service.CustomerUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CustomerUserDetailsServiceImpl implements CustomerUserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerUserDetailsServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
       var user = userRepository.findByEmail(email);
        if (user == null) {
            logger.warn("User not found: The user with email '{}' does not exist in the system.", email);
            throw new UsernameNotFoundException("User not found: The user with the provided email does not exist in the system.");
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("USER")
                .build();
    }
}
