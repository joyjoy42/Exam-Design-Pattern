package com.badwallet.api.entity;

/** How a deposit is funded. Drives which {@code PaymentMethodStrategy} handles it. */
public enum PaymentMethod {
    CREDIT_CARD,
    WALLET_TARGET
}
