package com.badwallet.api.service.payment;

import com.badwallet.api.dto.PaymentResultDto;
import com.badwallet.api.entity.TransactionType;
import com.badwallet.api.entity.Wallet;
import com.badwallet.api.event.TransactionRecordedEvent;
import com.badwallet.api.exception.InsufficientBalanceException;
import com.badwallet.api.repository.WalletRepository;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Template Method: paying "this month's bill" (1.9) and paying specific factures (1.10)
 * share the exact same skeleton — resolve how much is owed, debit the wallet, confirm
 * the payment with payment-service, then record it — and only differ in how the amount
 * due is resolved and which provider call settles it. Each concrete subclass is a
 * short-lived, per-request object built by {@code WalletService} (not a Spring bean)
 * since it carries that one request's parameters as instance state.
 */
public abstract class AbstractBillPaymentProcessor {

    private final WalletRepository walletRepository;
    private final ApplicationEventPublisher eventPublisher;

    protected AbstractBillPaymentProcessor(WalletRepository walletRepository, ApplicationEventPublisher eventPublisher) {
        this.walletRepository = walletRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Runs the shared skeleton against the resolved wallet. Holds the DB transaction
     * open across the call to payment-service — acceptable for this exam's scale, but
     * not something to copy into a system with real network latency/SLAs.
     */
    public final Wallet pay(Wallet wallet) {
        BigDecimal amountDue = resolveAmountDue(wallet);
        if (wallet.getBalance().compareTo(amountDue) < 0) {
            throw new InsufficientBalanceException(
                    "Solde insuffisant: " + wallet.getBalance() + " < " + amountDue);
        }

        wallet.debit(amountDue);
        walletRepository.save(wallet);

        PaymentResultDto result = confirmWithProvider(wallet, amountDue);

        List<String> settledReferences = new ArrayList<>(result.paidReferences());
        settledReferences.addAll(result.partiallyPaidReferences());

        eventPublisher.publishEvent(new TransactionRecordedEvent(
                wallet, TransactionType.PAYMENT, amountDue, BigDecimal.ZERO,
                null, null, serviceName(), settledReferences.isEmpty() ? null : String.join(",", settledReferences)));

        return wallet;
    }

    protected abstract BigDecimal resolveAmountDue(Wallet wallet);

    protected abstract PaymentResultDto confirmWithProvider(Wallet wallet, BigDecimal amountDue);

    protected abstract String serviceName();
}
