package com.walletcore.repository;

import com.walletcore.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    //Kiểm tra tồn tại trong db
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    //kiểm tra tài khoản tồn tại
    Optional<User> findByUsername(String username);
}
