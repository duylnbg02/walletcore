package com.walletcore.dto.request;

import lombok.Data;

@Data
public class TransactionsRequest {
    private String type;
    private String status;
    private Integer page = 0;
    private Integer size = 10;
}
