package com.walletcore.controller;

import com.walletcore.dto.request.AuthRequest;
import com.walletcore.dto.request.RegisterRequest;
import com.walletcore.dto.response.AuthResponse;
import com.walletcore.dto.response.RegisterResponse;
import com.walletcore.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public RegisterResponse register( @Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody AuthRequest request){
        return authService.login(request);
    }
}
