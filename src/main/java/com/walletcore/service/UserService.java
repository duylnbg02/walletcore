package com.walletcore.service;

import com.walletcore.dto.request.SetupPinRequest;

import java.util.UUID;

public interface UserService {
    void setupPin(UUID userId, SetupPinRequest request);

    void verifyPin(UUID userId, String pin);
}
