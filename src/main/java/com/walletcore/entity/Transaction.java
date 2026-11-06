package com.walletcore.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID senderWalletId;

    private UUID receiverWalletId;

    private String type;

    private Long amount;

    private String status;

    private String description;

    private Date createdAt;

}
