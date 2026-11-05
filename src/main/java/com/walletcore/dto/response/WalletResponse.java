package com.walletcore.dto.response;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class WalletResponse {
    private UUID id;
    private UUID userId;
    private Long balance;
    private String currency;
    private String status;
    private Date createdAt;
}
