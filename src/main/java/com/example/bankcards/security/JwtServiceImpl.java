package com.example.bankcards.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.Date;

@Slf4j
@Service
public class JwtServiceImpl implements JwtService {

    private final Key signingKey;
    private final long expirationMs;
    private final Clock clock;

    public JwtServiceImpl(
            @Value("${jwt.secret.base64}") String secretBase64,
            @Value("${jwt.expiration-ms:3600000}") long expirationMs,
            Clock clock
    ) {
        byte[] secret = Base64.getDecoder().decode(secretBase64);
        this.signingKey = Keys.hmacShaKeyFor(secret);
        this.expirationMs = expirationMs;
        this.clock = clock;
        log.info("JwtService initialized (expirationMs={})", expirationMs);
    }

    @Override
    public String generateToken(String username, List<String> roles) {
        Instant now = clock.instant();
        String token = Jwts.builder()
                .setSubject(username)
                .claim("roles", roles.toArray(new String[0]))
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
        log.debug("JWT issued for user={}", username);
        return token;
    }

    @Override
    public boolean isTokenValid(String token) {
        if (token == null || token.isBlank()) {
            log.debug("Token is null or blank");
            return false;
        }
        try {
            parseToken(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.debug("Token expired={}", ex.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Invalid token={}", ex.getMessage());
            return false;
        }
    }

    @Override
    public Optional<String> getUsernameOptional(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.ofNullable(parseToken(token).getBody().getSubject());
        } catch (JwtException ex) {
            log.debug("getUsername failed: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public List<String> getRoles(String token) {
        if (token == null || token.isBlank()) {
            return Collections.emptyList();
        }
        try {
            Claims body = parseToken(token).getBody();

            String[] arr = body.get("roles", String[].class);

            if (arr != null && arr.length > 0) {
                return Arrays.asList(arr);
            }

            List<?> raw = body.get("roles", List.class);
            if (raw == null || raw.isEmpty()) {
                return Collections.emptyList();
            }
            return raw.stream().map(Object::toString).toList();
        } catch (JwtException ex) {
            log.debug("getRoles failed: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    private Jws<Claims> parseToken(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setClock(() -> Date.from(clock.instant()))
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token);
    }
}

