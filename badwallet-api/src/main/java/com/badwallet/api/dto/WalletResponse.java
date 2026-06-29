package com.badwallet.api.dto;

import com.badwallet.api.entity.Wallet;

import java.math.BigDecimal;
import java.time.Instant;

public record WalletResponse(
        Long id,
        String code,
        String phoneNumber,
        String email,
        BigDecimal balance,
        String currency,
        Instant createdAt
) {
    public static WalletResponse from(Wallet wallet) {
        return new WalletResponse(
                wallet.getId(),
                wallet.getCode(),
                wallet.getPhoneNumber(),
                wallet.getEmail(),
                wallet.getBalance(),
                wallet.getCurrency(),
                wallet.getCreatedAt());
    }
}
