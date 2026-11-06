package com.walletcore.dto.response;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class WithdrawResponse {
    private UUID transactionId;
    private Long amount;
    private Long balance;
    private String status;
    private Date createdAt;
}
