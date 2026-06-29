package com.badwallet.api.service;

import com.badwallet.api.dto.CreateWalletRequest;
import com.badwallet.api.dto.DepositRequest;
import com.badwallet.api.dto.TransferRequest;
import com.badwallet.api.dto.TransferResponse;
import com.badwallet.api.dto.WithdrawRequest;
import com.badwallet.api.entity.Transaction;
import com.badwallet.api.entity.TransactionType;
import com.badwallet.api.entity.Wallet;
import com.badwallet.api.event.TransactionRecordedEvent;
import com.badwallet.api.exception.DuplicateWalletException;
import com.badwallet.api.exception.InsufficientBalanceException;
import com.badwallet.api.exception.WalletNotFoundException;
import com.badwallet.api.repository.TransactionRepository;
import com.badwallet.api.repository.WalletRepository;
import com.badwallet.api.service.fee.WithdrawalFeeStrategy;
import com.badwallet.api.service.strategy.PaymentStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Facade over wallet persistence and the transaction/payment machinery added by later
 * feature branches. Controllers only ever talk to this class, never to repositories or
 * strategies directly.
 */
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentStrategyFactory paymentStrategyFactory;
    private final WithdrawalFeeStrategy withdrawalFeeStrategy;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Wallet create(CreateWalletRequest request) {
        if (walletRepository.existsByPhoneNumber(request.phoneNumber())) {
            throw new DuplicateWalletException("Un portefeuille existe déjà pour " + request.phoneNumber());
        }
        if (walletRepository.existsByCode(request.code())) {
            throw new DuplicateWalletException("Le code de portefeuille " + request.code() + " est déjà utilisé");
        }

        Wallet wallet = Wallet.builder()
                .code(request.code())
                .phoneNumber(request.phoneNumber())
                .email(request.email())
                .balance(request.initialBalance())
                .currency(request.currency())
                .createdAt(Instant.now())
                .build();

        return walletRepository.save(wallet);
    }

    public Page<Wallet> list(Pageable pageable) {
        return walletRepository.findAll(pageable);
    }

    public Wallet getByPhoneNumber(String phoneNumber) {
        return walletRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new WalletNotFoundException("Aucun portefeuille pour " + phoneNumber));
    }

    @Transactional
    public Wallet deposit(Long walletId, DepositRequest request) {
        Wallet wallet = getById(walletId);
        paymentStrategyFactory.get(request.paymentMethod()).validate(request.amount());

        wallet.credit(request.amount());
        walletRepository.save(wallet);

        eventPublisher.publishEvent(new TransactionRecordedEvent(
                wallet, TransactionType.DEPOSIT, request.amount(), BigDecimal.ZERO,
                null, request.paymentMethod().name(), null, null));
        return wallet;
    }

    @Transactional
    public Wallet withdraw(WithdrawRequest request) {
        Wallet wallet = getByPhoneNumber(request.phoneNumber());
        BigDecimal fee = withdrawalFeeStrategy.calculateFee(request.amount());
        BigDecimal total = request.amount().add(fee);

        if (wallet.getBalance().compareTo(total) < 0) {
            throw new InsufficientBalanceException(
                    "Solde insuffisant: " + wallet.getBalance() + " < " + total + " (montant + frais)");
        }

        wallet.debit(total);
        walletRepository.save(wallet);

        eventPublisher.publishEvent(new TransactionRecordedEvent(
                wallet, TransactionType.WITHDRAW, request.amount(), fee, null, null, null, null));
        return wallet;
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        if (request.senderPhone().equals(request.receiverPhone())) {
            throw new IllegalArgumentException("Le portefeuille émetteur et destinataire doivent être différents");
        }
        Wallet sender = getByPhoneNumber(request.senderPhone());
        Wallet receiver = getByPhoneNumber(request.receiverPhone());

        if (sender.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException(
                    "Solde insuffisant: " + sender.getBalance() + " < " + request.amount());
        }

        sender.debit(request.amount());
        receiver.credit(request.amount());
        walletRepository.save(sender);
        walletRepository.save(receiver);

        eventPublisher.publishEvent(new TransactionRecordedEvent(
                sender, TransactionType.TRANSFER_OUT, request.amount(), BigDecimal.ZERO,
                receiver.getPhoneNumber(), null, null, null));
        eventPublisher.publishEvent(new TransactionRecordedEvent(
                receiver, TransactionType.TRANSFER_IN, request.amount(), BigDecimal.ZERO,
                sender.getPhoneNumber(), null, null, null));

        return TransferResponse.of(sender, receiver);
    }

    public List<Transaction> getTransactionHistory(String phoneNumber) {
        getByPhoneNumber(phoneNumber); // 404s on unknown numbers before hitting the ledger
        return transactionRepository.findByWalletPhoneNumberOrderByCreatedAtDesc(phoneNumber);
    }

    Wallet getById(Long walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Aucun portefeuille avec l'id " + walletId));
    }
}
