package com.walletcore.controller;

import com.walletcore.dto.request.ChangePinRequest;
import com.walletcore.dto.request.SetupPinRequest;
import com.walletcore.entity.User;
import com.walletcore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/setup-pin")
    public ResponseEntity<Void> setupPin(
            @AuthenticationPrincipal User user,
            @RequestBody SetupPinRequest request
    ) {
        userService.setupPin(user.getId(), request);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/change-pin")
    public ResponseEntity<Void> changePin(
            @AuthenticationPrincipal User user,
            @RequestBody ChangePinRequest request
    ) {
        userService.changePin(user.getId(), request);
        return ResponseEntity.noContent().build();
    }
}
