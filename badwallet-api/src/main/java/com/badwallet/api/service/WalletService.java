package com.badwallet.api.service;

import com.badwallet.api.dto.CreateWalletRequest;
import com.badwallet.api.dto.DepositRequest;
import com.badwallet.api.entity.TransactionType;
import com.badwallet.api.entity.Wallet;
import com.badwallet.api.event.TransactionRecordedEvent;
import com.badwallet.api.exception.DuplicateWalletException;
import com.badwallet.api.exception.WalletNotFoundException;
import com.badwallet.api.repository.WalletRepository;
import com.badwallet.api.service.strategy.PaymentStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Facade over wallet persistence and the transaction/payment machinery added by later
 * feature branches. Controllers only ever talk to this class, never to repositories or
 * strategies directly.
 */
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final PaymentStrategyFactory paymentStrategyFactory;
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

    Wallet getById(Long walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Aucun portefeuille avec l'id " + walletId));
    }
}
