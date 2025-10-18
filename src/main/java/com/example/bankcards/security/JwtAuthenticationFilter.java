package com.example.bankcards.security;

import com.example.bankcards.exception.JwtAuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final DbUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        Optional<String> tokenOpt = safeExtractToken(req, res);
        if (tokenOpt.isEmpty()) {
            chain.doFilter(req, res);
            return;
        }

        String token = tokenOpt.get();

        if (!isTokenValid(token, req, res)) {
            return;
        }

        if (hasAuthentication()) {
            chain.doFilter(req, res);
            return;
        }

        Optional<String> usernameOpt = jwtService.getUsernameOptional(token);
        if (usernameOpt.isEmpty()) {
            sendUnauthorized(res, "Invalid token: no subject");
            return;
        }

        String username = usernameOpt.get();

        Optional<UserDetails> userDetailsOpt = loadUserDetails(username);
        if (userDetailsOpt.isEmpty()) {
            sendUnauthorized(res, "User not found");
            return;
        }

        setAuthentication(userDetailsOpt.get(), token);
        chain.doFilter(req, res);
    }

    private Optional<String> safeExtractToken(HttpServletRequest req, HttpServletResponse res) throws IOException {
        try {
            return Optional.ofNullable(extractToken(req));
        } catch (JwtAuthenticationException ex) {
            log.debug("extractToken failed={}", ex.getMessage());
            sendUnauthorized(res, "Invalid authorization header");
            return Optional.empty();
        }
    }

    private String extractToken(HttpServletRequest req) {
        String header = req.getHeader("Authorization");
        if (header == null || header.isBlank()) {
            return null;
        }
        if (!header.startsWith("Bearer ")) {
            throw new JwtAuthenticationException("Authorization header must start with Bearer");
        }
        String token = header.substring(7).trim();
        if (token.isEmpty()) {
            throw new JwtAuthenticationException("Bearer token is empty");
        }
        return token;
    }

    private boolean isTokenValid(String token, HttpServletRequest req, HttpServletResponse res) throws IOException {
        boolean isValid = jwtService.isTokenValid(token);
        if (!isValid) {
            log.debug("Token invalid/expired for={}", req.getRequestURI());
            sendUnauthorized(res, "Invalid or expired token");
        }
        return isValid;
    }

    private boolean hasAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication() != null;
    }

    private Optional<UserDetails> loadUserDetails(String username) {
        try {
            return Optional.of(userDetailsService.loadUserByUsername(username));
        } catch (UsernameNotFoundException ex) {
            log.warn("UserDetails load failed for={}: {}", username, ex.getMessage());
            return Optional.empty();
        }
    }

    private void setAuthentication(UserDetails userDetails, String token) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Authentication set for user {}", userDetails.getUsername());
    }

    private void sendUnauthorized(HttpServletResponse res, String message) throws IOException {
        res.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
    }
}


