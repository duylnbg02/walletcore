package com.walletcore.dto.request;

import lombok.Data;

@Data
public class ChangePinRequest {

    private String oldPin;
    private String newPin;
}
