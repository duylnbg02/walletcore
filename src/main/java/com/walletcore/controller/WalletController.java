package com.walletcore.controller;

import com.walletcore.dto.request.*;
import com.walletcore.dto.response.*;
import com.walletcore.entity.Transaction;
import com.walletcore.entity.User;
import com.walletcore.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletservice;

    @GetMapping("/me")
    public WalletResponse getMyWallet(@AuthenticationPrincipal User user){
        return walletservice.getMyWallet(user.getId());
    }

    @PostMapping("/deposit")
    public DepositResponse depositWallet(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody DepositRequest request
    ) {
        return walletservice.depositWallet(user.getId(), request);
    }

    @PostMapping("/transfer")
    public TransferResponse transferWallet(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody TransferRequest request
    ){
        return walletservice.transferWallet(user.getId(), request);
    }

    @PostMapping("/withdraw")
    public WithdrawResponse withdrawWallet(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody WithdrawRequest request
    ) {
        return walletservice.withdrawWallet(user.getId(), request);
    }

    @GetMapping("/transactions")
    public PageResponse<TransactionsResponse> transactionsWallet(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return walletservice.transactionsWallet(user.getId(), type, status, page, size );
    }

    @GetMapping("/transactions/{transactionId}")
    public  TransactionsResponse getTransactionDetail(
            @AuthenticationPrincipal User user,
            @PathVariable UUID transactionId
    ){
      return walletservice.getTransactionDetail(user.getId(), transactionId);
    }

    @PatchMapping("/status")
    public  WalletResponse updateWalletStatus (
            @AuthenticationPrincipal User user,
            @Valid @RequestParam String status
    ){
        return walletservice.updateWalletStatus(user.getId(), status);
    }
}
