package com.badwallet.api.service.payment;

import com.badwallet.api.dto.FactureDto;
import com.badwallet.api.dto.PaymentResultDto;
import com.badwallet.api.entity.Wallet;
import com.badwallet.api.repository.WalletRepository;
import com.badwallet.api.service.proxy.FactureServiceProxy;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;

/** 1.10: the caller names exact factures, so the amount due is whatever they still owe on those. */
public class SpecificFacturesPaymentProcessor extends AbstractBillPaymentProcessor {

    private final FactureServiceProxy factureServiceProxy;
    private final String serviceName;
    private final List<String> factureReferences;

    public SpecificFacturesPaymentProcessor(
            WalletRepository walletRepository,
            ApplicationEventPublisher eventPublisher,
            FactureServiceProxy factureServiceProxy,
            String serviceName,
            List<String> factureReferences) {
        super(walletRepository, eventPublisher);
        this.factureServiceProxy = factureServiceProxy;
        this.serviceName = serviceName;
        this.factureReferences = factureReferences;
    }

    @Override
    protected BigDecimal resolveAmountDue(Wallet wallet) {
        return factureServiceProxy.getFacturesByReferences(factureReferences).stream()
                .map(FactureDto::amountDue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    protected PaymentResultDto confirmWithProvider(Wallet wallet, BigDecimal amountDue) {
        return factureServiceProxy.payByReferences(factureReferences);
    }

    @Override
    protected String serviceName() {
        return serviceName;
    }
}
