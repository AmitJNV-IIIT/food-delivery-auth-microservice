package com.sapient.auth_service.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface CustomerUserDetailsService {
    UserDetails loadUserByUsername(String email) throws UsernameNotFoundException;
}
