package com.walletcore.service;

import com.walletcore.dto.request.DepositRequest;
import com.walletcore.dto.response.DepositResponse;
import com.walletcore.dto.response.WalletResponse;

import java.util.UUID;

public interface WalletService {
    WalletResponse getMyWallet(UUID userId);

    DepositResponse depositWallet(UUID id, DepositRequest request);
}
