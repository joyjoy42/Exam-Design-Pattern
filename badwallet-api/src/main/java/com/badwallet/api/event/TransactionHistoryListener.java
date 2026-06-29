package com.badwallet.api.event;

import com.badwallet.api.entity.Transaction;
import com.badwallet.api.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Observer: turns a {@link TransactionRecordedEvent} into a persisted ledger line.
 * Runs synchronously on the publishing thread (default Spring behaviour) so it stays
 * inside the same database transaction as the balance change it is recording.
 */
@Component
@RequiredArgsConstructor
public class TransactionHistoryListener {

    private final TransactionRepository transactionRepository;

    @EventListener
    public void onTransactionRecorded(TransactionRecordedEvent event) {
        Transaction transaction = Transaction.builder()
                .reference("TXN-" + UUID.randomUUID())
                .walletId(event.wallet().getId())
                .walletPhoneNumber(event.wallet().getPhoneNumber())
                .type(event.type())
                .amount(event.amount())
                .fee(event.fee())
                .balanceAfter(event.wallet().getBalance())
                .counterpartyPhone(event.counterpartyPhone())
                .paymentMethod(event.paymentMethod())
                .serviceName(event.serviceName())
                .factureReferences(event.factureReferences())
                .createdAt(Instant.now())
                .build();
        transactionRepository.save(transaction);
    }
}
