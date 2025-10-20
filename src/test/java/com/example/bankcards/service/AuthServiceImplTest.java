package com.example.bankcards.service;

import com.example.bankcards.dto.user.AuthRequest;
import com.example.bankcards.dto.user.AuthResponse;
import com.example.bankcards.dto.user.RegisterRequest;
import com.example.bankcards.entity.role.Role;
import com.example.bankcards.exception.AuthenticationFailedException;
import com.example.bankcards.exception.RoleNotFoundException;
import com.example.bankcards.exception.UsernameAlreadyExistsException;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtService;
import com.example.bankcards.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private AuthRequest authRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest("user", "password");
        registerRequest = new RegisterRequest("newuser", "newpassword");
    }

    @Test
    void authenticate_returnsAuthResponse_whenCredentialsValid() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "user", "password", List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(jwtService.generateToken("user", List.of("ROLE_USER"))).thenReturn("jwt-token");

        AuthResponse response = authService.authenticate(authRequest);

        assertThat(response.getUsername()).isEqualTo("user");
        assertThat(response.getRoles()).containsExactly("ROLE_USER");
        assertThat(response.getToken()).isEqualTo("jwt-token");
    }

    @Test
    void authenticate_throwsException_whenBadCredentials() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.authenticate(authRequest))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Invalid username or password");
    }

    @Test
    void authenticate_throwsException_whenOtherAuthError() {
        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new AuthenticationServiceException("Service down"));


        assertThatThrownBy(() -> authService.authenticate(authRequest))
                .isInstanceOf(AuthenticationFailedException.class)
                .hasMessage("Authentication error");
    }

    @Test
    void register_saveUser_whenValid() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("newpassword")).thenReturn("ENCODED");
        Role role = new Role(1L, "ROLE_USER");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(role));

        authService.register(registerRequest);

        verify(userRepository).save(argThat(user ->
                user.getUsername().equals("newuser")
                        && user.getPassword().equals("ENCODED")
                        && user.getRoles().contains(role)
        ));
    }

    @Test
    void register_throwsException_whenUsernameExists() {
        when(userRepository.existsByUsername("newuser")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(UsernameAlreadyExistsException.class);
    }

    @Test
    void register_throwsException_whenRoleNotFound() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("newpassword")).thenReturn("ENCODED");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(RoleNotFoundException.class);
    }
}
