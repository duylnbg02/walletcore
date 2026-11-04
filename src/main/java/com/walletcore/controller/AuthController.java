package com.walletcore.controller;

import com.walletcore.dto.request.RegisterRequest;
import com.walletcore.dto.response.RegisterReponse;
import com.walletcore.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
    public RegisterReponse register( @Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }
}
