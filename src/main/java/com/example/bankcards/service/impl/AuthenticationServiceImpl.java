package com.example.bankcards.service.impl;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.dto.AuthResponse;
import com.example.bankcards.exception.AuthenticationFailedException;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public AuthResponse authenticate(AuthRequest authRequest) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(auth);

            String username = auth.getName();
            List<String> roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            String token = jwtService.generateToken(username, roles);
            return new AuthResponse(token);
        } catch (BadCredentialsException ex) {
            log.warn("Authentication failed for user={}: {}", authRequest.getUsername(), ex.getMessage());
            throw new AuthenticationFailedException("Invalid username or password");
        } catch (AuthenticationException ex) {
            log.warn("Authentication error for user={}: {}", authRequest.getUsername(), ex.getMessage());
            throw new AuthenticationFailedException("Authentication error");
        }
    }
}
