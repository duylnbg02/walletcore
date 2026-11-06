package com.walletcore.controller;

import com.walletcore.dto.request.DepositRequest;
import com.walletcore.dto.request.TransferRequest;
import com.walletcore.dto.response.DepositResponse;
import com.walletcore.dto.response.TransferResponse;
import com.walletcore.dto.response.WalletResponse;
import com.walletcore.entity.User;
import com.walletcore.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
}
