package com.badwallet.api.service;

import com.badwallet.api.dto.CreateWalletRequest;
import com.badwallet.api.entity.Wallet;
import com.badwallet.api.exception.DuplicateWalletException;
import com.badwallet.api.exception.WalletNotFoundException;
import com.badwallet.api.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
