package com.badwallet.api.service.strategy;

import com.badwallet.api.entity.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Agent cash-in: no card network, so no transaction cap — only a sanity check on the amount itself. */
@Component
public class WalletTargetDepositStrategy implements PaymentMethodStrategy {

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.WALLET_TARGET;
    }

    @Override
    public void validate(BigDecimal amount) {
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Le montant du dépôt doit être positif");
        }
    }
}
