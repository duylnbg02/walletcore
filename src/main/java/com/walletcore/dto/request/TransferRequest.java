package com.walletcore.dto.request;

import lombok.Data;

import java.util.UUID;

@Data
public class TransferRequest {
    private UUID receiverWalletId; //ID ví nhận
    private Long amount;
    private String description;
}
