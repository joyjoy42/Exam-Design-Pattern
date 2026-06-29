package com.badwallet.api.service.strategy;

import com.badwallet.api.entity.PaymentMethod;

import java.math.BigDecimal;

/**
 * Strategy: validates a deposit amount according to how it is funded. Concrete
 * strategies are picked at runtime by {@link PaymentStrategyFactory}.
 */
public interface PaymentMethodStrategy {

    PaymentMethod supportedMethod();

    /** @throws IllegalArgumentException if the amount is not acceptable for this funding source. */
    void validate(BigDecimal amount);
}
