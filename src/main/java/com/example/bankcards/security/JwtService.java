package com.example.bankcards.security;

import java.util.List;
import java.util.Optional;

public interface JwtService {

    String generateToken(String username, List<String> roles);

    boolean isTokenValid(String token);

    Optional<String> getUsernameOptional(String token);

    List<String> getRoles(String token);
}
