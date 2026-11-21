package com.walletcore.service.impl;

import com.walletcore.dto.request.DepositRequest;
import com.walletcore.dto.request.TransferRequest;
import com.walletcore.dto.request.WithdrawRequest;
import com.walletcore.dto.response.*;
import com.walletcore.entity.Transaction;
import com.walletcore.entity.Wallet;
import com.walletcore.repository.TransactionRepository;
import com.walletcore.repository.WalletRepository;
import com.walletcore.service.RedisService;
import com.walletcore.service.WalletService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final RedisService redisService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public WalletResponse getMyWallet(UUID userId) {

        // TODO 1: Tạo Redis Key
        String key = "walletcore:wallet:user:" + userId;

        // TODO 2: Lấy dữ liệu từ Redis
        String cachedData = null; 
        try {
            cachedData = redisService.get(key);
        } catch (Exception e){
            log.warn("Không thể đọc Redis: {}", e.getMessage());
        }

        // TODO 3: Cache HIT - Redis có dữ liệu
        if (cachedData != null) {
            return objectMapper.readValue( cachedData, WalletResponse.class);
        }

        // TODO 4: Cache MISS - Lấy dữ liệu từ PostgreSQL
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví"));

        // TODO 5: Mapping Entity sang WalletResponse
        WalletResponse response = new WalletResponse();

        response.setId(wallet.getId());
        response.setUserId(wallet.getUserId());
        response.setBalance(wallet.getBalance());
        response.setCurrency(wallet.getCurrency());
        response.setStatus(wallet.getStatus());
        response.setCreatedAt(wallet.getCreatedAt());

        // TODO 6: Chuyển WalletResponse thành JSON
        String json = objectMapper.writeValueAsString(response);

        // TODO 7: Lưu dữ liệu vào Redis với TTL 60 giây
        try {
            redisService.set(key, json, Duration.ofSeconds(60));
        } catch (Exception e) {
            log.warn("Không thể ghi Redis: {}", e.getMessage());
        }

        // TODO 8: Trả về WalletResponse
        return response;
    }

    @Override
    @Transactional
    public DepositResponse depositWallet(UUID userId, DepositRequest request){

        // TODO 1: Check amount
        if(request.getAmount() < 10000){
            throw new RuntimeException("Giao dịch tối thiểu 10000 VNĐ");
        }
        // TODO 2: tìm wallet theo user id
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy tài khoản giao dịch"));
        // TODO 3: Wallet active
        if (!"ACTIVE".equals(wallet.getStatus())) {
            throw new RuntimeException("Tài khoản bị đóng băng");
        }
        // TODO 4: Update wallet
        wallet.setBalance(Math.addExact(wallet.getBalance(), request.getAmount()));
        walletRepository.save(wallet);

        // TODO 5: Lưu giao dịch (transaction)
        Transaction transaction = new Transaction();
        transaction.setSenderWalletId(null);
        transaction.setReceiverWalletId(wallet.getId());
        transaction.setType("DEPOSIT");
        transaction.setAmount(request.getAmount());
        transaction.setStatus("SUCCESS");
        transaction.setCreatedAt(new Date());

        transactionRepository.save(transaction);

        // TODO 6: xóa redis
        redisService.delete("walletcore:wallet:user:" + userId);

        // TODO 7: Trả reponse
        DepositResponse response = new DepositResponse();
        response.setTransactionId(transaction.getId());
        response.setAmount(transaction.getAmount());
        response.setBalance(wallet.getBalance());
        response.setStatus(transaction.getStatus());

        return response;
    }

    @Override
    @Transactional
    public TransferResponse transferWallet( UUID userId, TransferRequest request) {

        // TODO 1: Kiểm tra số tiền chuyển
        if (request.getAmount() == null || request.getAmount() < 10000) {
            throw new RuntimeException("Giao dịch tối thiểu 10000 VNĐ");
        }
        // TODO 2: Tìm ID ví gửi theo userId
        Wallet sender = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví gửi"));

        UUID senderId = sender.getId();
        UUID receiverId = request.getReceiverWalletId();

        if (receiverId == null) {
            throw new RuntimeException("ID ví nhận không được để trống");
        }

        // TODO 3: Không chuyển cho chính mình
        if (senderId.equals(receiverId)) {
            throw new RuntimeException("Không thể chuyển tiền cho chính mình");
        }

        UUID firstId = senderId.compareTo(receiverId) < 0
                ? senderId : receiverId;

        UUID secondId = senderId.compareTo(receiverId) < 0
                ? receiverId : senderId;

        Wallet firstWallet = walletRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví"));

        Wallet secondWallet = walletRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví"));

        Wallet walletSend = senderId.equals(firstId)
                ? firstWallet : secondWallet;

        Wallet walletReceiver = receiverId.equals(firstId)
                ? firstWallet : secondWallet;

        // TODO 4: Kiểm tra trạng thái hai ví ACTIVE
        if (!"ACTIVE".equals(walletReceiver.getStatus()) || !"ACTIVE".equals(walletSend.getStatus())) {
            throw new RuntimeException("Tài khoản bị đóng băng");
        }

        // TODO 5: Kiểm tra số dư ví gửi có đủ không
        if(walletSend.getBalance() < request.getAmount()){
            throw new RuntimeException("Số dư không đủ");
        }

        // TODO 6: Trừ tiền ví gửi
        walletSend.setBalance(Math.subtractExact(walletSend.getBalance(), request.getAmount()));
        walletRepository.save(walletSend);

        // TODO 7: Cộng tiền ví nhận
        walletReceiver.setBalance(Math.addExact(walletReceiver.getBalance(),request.getAmount()));
        walletRepository.save(walletReceiver);

        // TODO 8: Xóa Redis
        redisService.delete("walletcore:wallet:user:" + userId);

        redisService.delete("walletcore:wallet:user:" + walletReceiver.getUserId());

        // TODO 9: Lưu lịch sử giao dịch
        Transaction transaction = new Transaction();
        transaction.setSenderWalletId(walletSend.getId());
        transaction.setReceiverWalletId(walletReceiver.getId());
        transaction.setType("TRANSFER");
        transaction.setAmount(request.getAmount());
        transaction.setStatus("SUCCESS");
        transaction.setDescription(request.getDescription());
        transaction.setCreatedAt(new Date());

        transactionRepository.save(transaction);
        // TODO 10: Trả TransferResponse
        TransferResponse response = new TransferResponse();
        response.setSenderWalletId(transaction.getSenderWalletId());
        response.setReceiverWalletId(transaction.getReceiverWalletId());
        response.setTransactionId(transaction.getId());
        response.setAmount(transaction.getAmount());
        response.setBalance(walletSend.getBalance());
        response.setStatus(transaction.getStatus());
        response.setDescription(transaction.getDescription());
        response.setCreatedAt(transaction.getCreatedAt());

        return response;
    }

    @Transactional
    @Override
    public WithdrawResponse withdrawWallet (UUID userId, WithdrawRequest request){
        //TODO 1: Kiểm tra số tiền rút tối thiểu
        if (request.getAmount() == null || request.getAmount() < 10000) {
            throw new RuntimeException("Giao dịch tối thiểu 10000 VNĐ");
        }
        //TODO 2: Tìm ví theo userId lấy từ JWT và khóa ví.
        Wallet myWallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví"));

        Wallet wallet = walletRepository.findByIdForUpdate(myWallet.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ví"));

        //TODO 3: Kiểm tra ví ACTIVE.
        if (!"ACTIVE".equals(wallet.getStatus())) {
            throw new RuntimeException("Tài khoản bị đóng băng");
        }
        //TODO 4: Kiểm tra số dư có đủ không.
        if(wallet.getBalance() < request.getAmount()){
            throw new RuntimeException("Số dư không đủ");
        }
        //TODO 5: Trừ tiền
        wallet.setBalance(Math.subtractExact(wallet.getBalance(), request.getAmount()));
        walletRepository.save(wallet);

        //TODO 6: Xóa Redis
        redisService.delete("walletcore:wallet:user:" + userId);

        // TODO 7: Lưu Transaction
        Transaction transaction = new Transaction();
        transaction.setSenderWalletId(wallet.getId());
        transaction.setReceiverWalletId(null);
        transaction.setType("WITHDRAW");
        transaction.setAmount(request.getAmount());
        transaction.setStatus("SUCCESS");
        transaction.setCreatedAt(new Date());

        transactionRepository.save(transaction);
        //TODO 8: Response
        WithdrawResponse response = new WithdrawResponse();
        response.setTransactionId(transaction.getId());
        response.setAmount(transaction.getAmount());
        response.setBalance(wallet.getBalance());
        response.setStatus(transaction.getStatus());
        response.setCreatedAt(transaction.getCreatedAt());

        return response;
    }

    @Override
    @Transactional
    public PageResponse<TransactionsResponse> transactionsWallet( UUID userId, String type, String status, int page, int size) {
        // TODO 1: Tìm ví của user đang đăng nhập
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Không có Wallet hợp lệ"));

        // TODO 2: Tạo phân trang
        Pageable pageable = PageRequest.of( page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        // TODO 3: Lấy lịch sử giao dịch
        Page<Transaction> transactions = transactionRepository.findHistory( wallet.getId(), type, status, pageable
        );

        // TODO 4: Mapping Transaction sang TransactionsResponse
        Page<TransactionsResponse> responsePage = transactions.map(transaction -> {

            TransactionsResponse response = new TransactionsResponse();

            response.setId(transaction.getId());
            response.setType(transaction.getType());
            response.setAmount(transaction.getAmount());
            response.setStatus(transaction.getStatus());
            response.setDescription(transaction.getDescription());
            response.setCreatedAt(transaction.getCreatedAt());

            // TODO 5: Xác định chiều giao dịch
            response.setDirection(wallet.getId().equals(transaction.getSenderWalletId()) ? "OUT" : "IN");
            return response;
        });

        // TODO 6: Chuyển Page thành PageResponse
        return new PageResponse<>(
                responsePage.getContent(),
                responsePage.getTotalElements(),
                responsePage.getNumber(),
                responsePage.getSize()
        );
    }

    @Override
    @Transactional
    public TransactionsResponse getTransactionDetail (UUID userId, UUID transactionId ){
        // TODO 1: Tìm Wallet theo userId
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Không có Wallet hợp lệ"));

        // TODO 2: Tìm Transaction theo transactionId
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Id giao dịch không hợp lệ"));

        // TODO 3: Kiểm tra walletId có phải sender hoặc receiver
        boolean isSender = wallet.getId().equals(transaction.getSenderWalletId());

        boolean isReceiver = wallet.getId().equals(transaction.getReceiverWalletId());

        if (!isSender && !isReceiver) {
            throw new RuntimeException("Bạn không có quyền xem giao dịch này");
        }

        // TODO 4: Xác định direction IN / OUT
        String direction = isSender ? "OUT" : "IN";

        // TODO 5: Mapping Transaction sang TransactionsResponse
        TransactionsResponse response = new TransactionsResponse();

        response.setId(transaction.getId());
        response.setType(transaction.getType());
        response.setDirection(direction);
        response.setAmount(transaction.getAmount());
        response.setStatus(transaction.getStatus());
        response.setDescription(transaction.getDescription());
        response.setCreatedAt(transaction.getCreatedAt());

        return response;
    }

    @Override
    @Transactional
    public WalletResponse updateWalletStatus(UUID userId, String status) {
        if (!"ACTIVE".equals(status) && !"FROZEN".equals(status)) {
            throw new IllegalArgumentException("Trạng thái ví không hợp lệ");
        }

        // TODO 2: Tìm Wallet theo userId
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Không có Wallet hợp lệ"));

        // TODO 3: Kiểm tra trạng thái hiện tại
        if (wallet.getStatus().equals(status)) {
            throw new IllegalStateException("Ví đã ở trạng thái " + status);
        }

        // TODO 4: Cập nhật trạng thái
        wallet.setStatus(status);

        // TODO 5: Lưu vào database
        walletRepository.save(wallet);

        redisService.delete("walletcore:wallet:user:" + userId);

        // TODO 6: Mapping sang WalletResponse
        WalletResponse response = new WalletResponse();
        response.setId(wallet.getId());
        response.setUserId(wallet.getUserId());
        response.setBalance(wallet.getBalance());
        response.setCurrency(wallet.getCurrency());
        response.setStatus(wallet.getStatus());
        response.setCreatedAt(wallet.getCreatedAt());

        // TODO 7: Trả kết quả
        return response;
    }
}
