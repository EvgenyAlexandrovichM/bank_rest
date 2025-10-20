package com.example.bankcards.service.impl;

import com.example.bankcards.dto.user.AuthRequest;
import com.example.bankcards.dto.user.AuthResponse;
import com.example.bankcards.dto.user.RegisterRequest;
import com.example.bankcards.entity.role.Role;
import com.example.bankcards.entity.user.User;
import com.example.bankcards.exception.AuthenticationFailedException;
import com.example.bankcards.exception.RoleNotFoundException;
import com.example.bankcards.exception.UsernameAlreadyExistsException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse authenticate(AuthRequest authRequest) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            String username = auth.getName();
            List<String> roles = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            String token = jwtService.generateToken(username, roles);
            return new AuthResponse(token, username, roles);
        } catch (BadCredentialsException ex) {
            log.warn("Authentication failed for user={}: {}", authRequest.getUsername(), ex.getMessage());
            throw new AuthenticationFailedException("Invalid username or password");
        } catch (AuthenticationException ex) {
            log.warn("Authentication error for user={}: {}", authRequest.getUsername(), ex.getMessage());
            throw new AuthenticationFailedException("Authentication error");
        }
    }

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            log.warn("User with name={} already exists", request.getUsername());
            throw new UsernameAlreadyExistsException(request.getUsername());
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        user.setCreatedAt(Instant.now());

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RoleNotFoundException("ROLE_USER"));

        user.setRoles(Set.of(userRole));

        userRepository.save(user);

        log.info("New user registered={}", user.getUsername());
    }
}
