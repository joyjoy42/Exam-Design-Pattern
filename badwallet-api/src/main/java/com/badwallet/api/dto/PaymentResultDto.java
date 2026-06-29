package com.badwallet.api.dto;

import java.math.BigDecimal;
import java.util.List;

/** Mirrors payment-service's PaymentResult: outcome of a payment applied to one or more factures. */
public record PaymentResultDto(
        BigDecimal amountApplied,
        BigDecimal remainingDue,
        List<String> paidReferences,
        List<String> partiallyPaidReferences
) {
}
