package com.walletcore.service;

import com.walletcore.dto.request.RegisterRequest;
import com.walletcore.dto.response.RegisterReponse;

public interface AuthService {
    RegisterReponse register(RegisterRequest request);
}