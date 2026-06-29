package com.badwallet.api.dto;

import com.badwallet.api.entity.Transaction;
import com.badwallet.api.entity.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        String reference,
        TransactionType type,
        BigDecimal amount,
        BigDecimal fee,
        BigDecimal balanceAfter,
        String counterpartyPhone,
        String paymentMethod,
        String serviceName,
        String factureReferences,
        Instant createdAt
) {
    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.getReference(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getFee(),
                transaction.getBalanceAfter(),
                transaction.getCounterpartyPhone(),
                transaction.getPaymentMethod(),
                transaction.getServiceName(),
                transaction.getFactureReferences(),
                transaction.getCreatedAt());
    }
}
