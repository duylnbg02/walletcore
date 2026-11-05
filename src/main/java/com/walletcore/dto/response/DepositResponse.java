package com.walletcore.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class DepositResponse {
    private UUID transactionId;
    private Long amount;
    private Long balance;
    private String status;
}
