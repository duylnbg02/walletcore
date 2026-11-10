package com.walletcore.dto.response;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class TransactionsResponse {
    private UUID id;
    private String type;
    private String direction;
    private Long amount;
    private String status;
    private String description;
    private Date createdAt;
}
