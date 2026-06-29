package com.badwallet.api.event;

import com.badwallet.api.entity.TransactionType;
import com.badwallet.api.entity.Wallet;

import java.math.BigDecimal;

/**
 * Published by {@code WalletService} right after a wallet's balance is mutated.
 * Decouples "what happened to the money" (the operation itself) from "recording it"
 * (see {@code TransactionHistoryListener}), so future side-effects (notifications,
 * fraud checks, analytics) can subscribe without touching the operation code.
 *
 * <p>Carries the wallet's post-operation state; the listener snapshots
 * {@code wallet.getBalance()} as the ledger line's {@code balanceAfter}.
 */
public record TransactionRecordedEvent(
        Wallet wallet,
        TransactionType type,
        BigDecimal amount,
        BigDecimal fee,
        String counterpartyPhone,
        String paymentMethod,
        String serviceName,
        String factureReferences
) {
    public static TransactionRecordedEvent of(Wallet wallet, TransactionType type, BigDecimal amount) {
        return new TransactionRecordedEvent(wallet, type, amount, BigDecimal.ZERO, null, null, null, null);
    }
}
