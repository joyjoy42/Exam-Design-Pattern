package com.badwallet.api.service.payment;

import com.badwallet.api.dto.PaymentResultDto;
import com.badwallet.api.entity.Wallet;
import com.badwallet.api.repository.WalletRepository;
import com.badwallet.api.service.proxy.FactureServiceProxy;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;

/** 1.9: the caller picks how much to pay toward the current month's bill — no lookup needed. */
public class CurrentMonthPaymentProcessor extends AbstractBillPaymentProcessor {

    private final FactureServiceProxy factureServiceProxy;
    private final String serviceName;
    private final BigDecimal requestedAmount;

    public CurrentMonthPaymentProcessor(
            WalletRepository walletRepository,
            ApplicationEventPublisher eventPublisher,
            FactureServiceProxy factureServiceProxy,
            String serviceName,
            BigDecimal requestedAmount) {
        super(walletRepository, eventPublisher);
        this.factureServiceProxy = factureServiceProxy;
        this.serviceName = serviceName;
        this.requestedAmount = requestedAmount;
    }

    @Override
    protected BigDecimal resolveAmountDue(Wallet wallet) {
        return requestedAmount;
    }

    @Override
    protected PaymentResultDto confirmWithProvider(Wallet wallet, BigDecimal amountDue) {
        return factureServiceProxy.payCurrentMonth(wallet.getCode(), serviceName, amountDue);
    }

    @Override
    protected String serviceName() {
        return serviceName;
    }
}
