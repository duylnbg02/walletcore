package com.walletcore.repository;

import com.walletcore.entity.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    // Tìm ví theo userId, không khóa
    Optional<Wallet> findByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.userId = :userId")
    Optional<Wallet> findByUserIdForUpdate(@Param("userId") UUID userId);

    // Chỉ lấy ID để không đưa số dư chưa khóa vào persistence context.
    @Query("SELECT w.id FROM Wallet w WHERE w.userId = :userId")
    Optional<UUID> findIdByUserId(@Param("userId") UUID userId);

    // Tìm ví theo ID và khóa dòng dữ liệu
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdForUpdate(@Param("id") UUID id);
}
