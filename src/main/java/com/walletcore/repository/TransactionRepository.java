package com.walletcore.repository;

import com.walletcore.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

        @Query("""
        SELECT t FROM Transaction t
        WHERE (t.senderWalletId = :walletId
            OR t.receiverWalletId = :walletId)
          AND (:type IS NULL OR t.type = :type)
          AND (:status IS NULL OR t.status = :status)
    """)
        Page<Transaction> findHistory(
                @Param("walletId") UUID walletId,
                @Param("type") String type,
                @Param("status") String status,
                Pageable pageable
        );
    }
