package com.sapient.auth_service.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class AuthResponse {
    private String jwt;
    private String message;
    private boolean isValid;

}
