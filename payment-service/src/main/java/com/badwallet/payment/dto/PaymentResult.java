package com.badwallet.payment.dto;

import java.math.BigDecimal;
import java.util.List;

/** Outcome of applying a payment against one or more factures. */
public record PaymentResult(
        BigDecimal amountApplied,
        BigDecimal remainingDue,
        List<String> paidReferences,
        List<String> partiallyPaidReferences
) {
}
