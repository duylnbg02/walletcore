package com.walletcore.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_name")
    private String username;

    private String email;

    private String phone;

    private String roleCode;

    private String passwordHash;

    private String status;

    private Date createdAt;

}
