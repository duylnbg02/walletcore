package com.walletcore.service.impl;

import com.walletcore.dto.request.AuthRequest;
import com.walletcore.dto.request.RegisterRequest;
import com.walletcore.dto.response.AuthResponse;
import com.walletcore.dto.response.RegisterResponse;
import com.walletcore.entity.User;
import com.walletcore.entity.Wallet;
import com.walletcore.repository.UserRepository;
import com.walletcore.repository.WalletRepository;
import com.walletcore.security.JwtService;
import com.walletcore.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor //Tự động tạo Constructor
// implement là từ khóa được sử dụng để xác lập mối quan hệ giữa một Class và một Interface.
public class AuthServiceImpl implements AuthService {

    private final JwtService jwtTokenProvider;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;

    @Override // Đánh dấu triển khai/ghi đè
    @Transactional //Quản lý giao dịch Database
    public RegisterResponse register(RegisterRequest request) {

        // TODO 1: Kiểm tra username tồn tại
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Tên đăng nhập dã tồn tại");
        }

        // TODO 2: Kiểm tra email tồn tại
        if (userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Tài khoản email đã được đăng ký");
        }

        // TODO 3: Kiểm tra phone tồn tại
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Số điện thoại này đã được đăng ký");
        }

        // TODO 4: Mã hóa password bằng BCrypt
        String hashPassword = passwordEncoder.encode(request.getPassword());

        // TODO 5: Tạo và lưu User
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPasswordHash(hashPassword);
        user.setStatus("ACTIVE");
        user.setRoleCode("USER");
        user.setCreatedAt(new Date());
        userRepository.save(user);

        // TODO 6: Tạo Wallet với balance = 0
        Wallet wallet = new Wallet();
        wallet.setUserId(user.getId());
        wallet.setBalance(0L);
        wallet.setStatus("ACTIVE");
        wallet.setCurrency("VND");
        wallet.setVersion(0L);
        wallet.setCreatedAt(new Date());
        walletRepository.save(wallet);

        // TODO 7: Trả về RegisterResponse
        RegisterResponse response = new RegisterResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setRoleCode(user.getRoleCode());
        response.setWalletId(wallet.getId());

        return response;
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        // TODO 1: Tìm Username
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));
        // TODO 2: Kiểm tra password bằng BCrypt
        if(!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())){
            throw new RuntimeException("Tên đăng nhập hoặc mật khẩu không chính xác");
        }
        // TODO 3: Kiểm tra trạng thái tài khoản ACTIVE
        if(!"ACTIVE".equals(user.getStatus())){
            throw new RuntimeException("Tài khoản đã bị khóa");
        }
        // TODO 4: Tạo JWT Access Token
        String accessToken = jwtTokenProvider.generateToken(user);
        long expiresIn = 3600L;

        // TODO 5: Trả về AuthResponse
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setTokenType("Bearer");
        response.setExpiresIn(expiresIn);
        return response;
    }
}
