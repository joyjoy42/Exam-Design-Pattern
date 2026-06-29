package com.badwallet.api.service.strategy;

import com.badwallet.api.entity.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Card networks cap single-transaction amounts; agent cash-in (WALLET_TARGET) does not. */
@Component
public class CreditCardDepositStrategy implements PaymentMethodStrategy {

    private static final BigDecimal MAX_CARD_DEPOSIT = BigDecimal.valueOf(1_000_000);

    @Override
    public PaymentMethod supportedMethod() {
        return PaymentMethod.CREDIT_CARD;
    }

    @Override
    public void validate(BigDecimal amount) {
        if (amount.compareTo(MAX_CARD_DEPOSIT) > 0) {
            throw new IllegalArgumentException(
                    "Le montant dépasse la limite autorisée pour un dépôt par carte (" + MAX_CARD_DEPOSIT + ")");
        }
    }
}
