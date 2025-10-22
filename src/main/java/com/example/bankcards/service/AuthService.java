package com.example.bankcards.service;

import com.example.bankcards.dto.user.AuthRequest;
import com.example.bankcards.dto.user.AuthResponse;
import com.example.bankcards.dto.user.RegisterRequest;

public interface AuthService {

    AuthResponse authenticate(AuthRequest authRequest);

    void register(RegisterRequest request);
}
