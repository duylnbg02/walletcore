package com.walletcore.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class RegisterResponse {
    private UUID id;
    private UUID walletId;
    private String username;
    private String email;
    private String phone;
    private String roleCode;
}
