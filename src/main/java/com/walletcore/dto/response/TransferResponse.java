package com.walletcore.dto.response;

import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class TransferResponse {

    private UUID transactionId;
    private UUID senderWalletId; //ID ví gửi
    private UUID receiverWalletId; //ID ví nhận
    private Long amount;
    private Long balance;
    private String status;
    private String description;
    private Date createdAt;

}
