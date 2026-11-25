package com.walletcore.service.impl;

import com.walletcore.dto.request.SetupPinRequest;
import com.walletcore.entity.User;
import com.walletcore.repository.UserRepository;
import com.walletcore.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
@Transactional
public void setupPin(UUID userId, SetupPinRequest request) {
    String pin = request.getPin();
    validatePin(pin);

    User user = userRepository.findByIdForUpdate(userId)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Không tìm thấy người dùng"
            ));

    if (user.getPinHash() != null) {
        throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Bạn đã thiết lập mã PIN"
        );
    }

    String pinHash = passwordEncoder.encode(pin);
    user.setPinHash(pinHash);
    userRepository.save(user);
}

    @Override
    public void verifyPin(UUID userId, String pin) {
        validatePin(pin);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Không tìm thấy người dùng"));

        if (user.getPinHash() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Bạn chưa thiết lập mã PIN");
        }

        if (!passwordEncoder.matches(pin, user.getPinHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mã PIN không chính xác");
        }
    }

    private void validatePin(String pin) {
        if (pin == null || pin.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PIN không được để trống");
        }
        if (!pin.matches("^[0-9]{6}$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PIN phải gồm đúng 6 chữ số");
        }
    }
}
