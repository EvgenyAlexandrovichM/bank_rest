package com.example.bankcards.controller;


import com.example.bankcards.dto.user.AuthRequest;
import com.example.bankcards.dto.user.AuthResponse;
import com.example.bankcards.dto.user.RegisterRequest;
import com.example.bankcards.exception.AuthenticationFailedException;
import com.example.bankcards.exception.UsernameAlreadyExistsException;
import com.example.bankcards.security.JwtAuthenticationFilter;
import com.example.bankcards.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class))
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @WithMockUser(roles = "USER")
    void login_returns200_andAuthResponse_whenValid() throws Exception {
        AuthRequest request = new AuthRequest("user", "pass");
        AuthResponse response = new AuthResponse("jwt-token", "user", List.of("ROLE_USER"));

        when(authService.authenticate(any(AuthRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void login_returns401_whenAuthFails() throws Exception {
        AuthRequest request = new AuthRequest("user", "wrong");

        when(authService.authenticate(any(AuthRequest.class)))
                .thenThrow(new AuthenticationFailedException("Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void register_returns201_whenValid() throws Exception {
        RegisterRequest request = new RegisterRequest("newuser", "P@ssw0rd!");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void register_returns409_whenUsernameExists() throws Exception {
        RegisterRequest request = new RegisterRequest("user", "P@ssw0rd!");

        doThrow(new UsernameAlreadyExistsException("user"))
                .when(authService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    void register_returns400_whenPasswordInvalid() throws Exception {
        RegisterRequest request = new RegisterRequest("newuser", "password");

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("Invalid password")))
                .andExpect(jsonPath("$.path").value("/api/auth/register"));
    }
}