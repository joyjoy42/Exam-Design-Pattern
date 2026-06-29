package com.badwallet.api.dto;

import com.badwallet.api.entity.Wallet;

import java.math.BigDecimal;

public record BalanceResponse(String phoneNumber, BigDecimal balance, String currency) {
    public static BalanceResponse from(Wallet wallet) {
        return new BalanceResponse(wallet.getPhoneNumber(), wallet.getBalance(), wallet.getCurrency());
    }
}
