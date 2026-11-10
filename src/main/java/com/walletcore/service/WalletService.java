package com.walletcore.service;

import com.walletcore.dto.request.DepositRequest;
import com.walletcore.dto.request.TransactionsRequest;
import com.walletcore.dto.request.TransferRequest;
import com.walletcore.dto.request.WithdrawRequest;
import com.walletcore.dto.response.*;
import org.springframework.data.domain.Page;

import java.util.Map;
import java.util.UUID;

public interface WalletService {
    WalletResponse getMyWallet(UUID userId);

    DepositResponse depositWallet(UUID id, DepositRequest request);

    TransferResponse transferWallet(UUID userID, TransferRequest request);

    WithdrawResponse withdrawWallet(UUID userID, WithdrawRequest request);

    PageResponse<TransactionsResponse> transactionsWallet( UUID userId,  String type,  String status,  int page,  int size );
}
