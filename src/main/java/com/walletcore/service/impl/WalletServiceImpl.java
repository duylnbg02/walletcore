package com.walletcore.service.impl;

import com.walletcore.dto.request.DepositRequest;
import com.walletcore.dto.response.DepositResponse;
import com.walletcore.dto.response.WalletResponse;
import com.walletcore.entity.Transaction;
import com.walletcore.entity.Wallet;
import com.walletcore.repository.TransactionRepository;
import com.walletcore.repository.WalletRepository;
import com.walletcore.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    @Override
    @Transactional
    public WalletResponse getMyWallet(UUID userId){
        // TODO 1: Tìm ví theo userId
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(()->new RuntimeException("Không tìm thấy ví"));
        // TODO 2: Chuyển Entity sang Response
        WalletResponse response = new WalletResponse();
        response.setId(wallet.getId());
        response.setUserId(wallet.getUserId());
        response.setBalance(wallet.getBalance());
        response.setCurrency(wallet.getCurrency());
        response.setStatus(wallet.getStatus());
        response.setCreatedAt(wallet.getCreatedAt());
        return response;
    }

    @Override
    @Transactional
    public DepositResponse depositWallet(UUID userId, DepositRequest request){

        // TODO 1: Check amount
        if(request.getAmount() < 10000){
            throw new RuntimeException("Giao dịch tối thiểu 10000 VNĐ");
        }
        // TODO 2: tìm wallet theo user id
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy tài khoản giao dịch"));
        // TODO 3: Wallet active
        if (!"ACTIVE".equals(wallet.getStatus())) {
            throw new RuntimeException("Tài khoản bị đóng băng");
        }
        // TODO 4: Update wallet
        wallet.setBalance(Math.addExact(wallet.getBalance(), request.getAmount()));
        walletRepository.save(wallet);

        // TODO 5: Lưu giao dịch (transaction)
        Transaction transaction = new Transaction();
        transaction.setWalletId(wallet.getId());
        transaction.setType("DEPOSIT");
        transaction.setAmount(request.getAmount());
        transaction.setStatus("SUCCESS");
        transaction.setCreatedAt(new Date());

        transactionRepository.save(transaction);
        // TODO 6: Trả reponse
        DepositResponse response = new DepositResponse();
        response.setTransactionId(transaction.getId());
        response.setAmount(transaction.getAmount());
        response.setBalance(wallet.getBalance());
        response.setStatus(transaction.getStatus());

        return response;
    }
}
