package com.walletcore.service;

import com.walletcore.dto.request.AuthRequest;
import com.walletcore.dto.request.RegisterRequest;
import com.walletcore.dto.response.AuthResponse;
import com.walletcore.dto.response.RegisterResponse;

public interface AuthService {
    RegisterResponse register(RegisterRequest request);
    AuthResponse login(AuthRequest request);
}