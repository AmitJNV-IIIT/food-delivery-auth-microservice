package com.sapient.auth_service.util;

public class JwtConstant {

    // Private constructor to prevent instantiation
    private JwtConstant() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    public static final String SECRET_KEY = "jhbhcbchbuirck3kij3jebjmcdeknikeckencjncmcc"; // at least 256 bits or 32 chars if ascii
    public static final String JWT_HEADER = "Authorization";
}
