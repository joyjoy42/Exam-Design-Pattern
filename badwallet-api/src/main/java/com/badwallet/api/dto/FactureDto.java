package com.badwallet.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Mirrors payment-service's facture representation. Kept as plain strings for
 * serviceName/status so the two services don't have to share enum classes. */
public record FactureDto(
        String reference,
        String walletCode,
        String serviceName,
        BigDecimal amount,
        BigDecimal amountDue,
        String status,
        int periodMonth,
        int periodYear,
        LocalDate issuedAt
) {
}
