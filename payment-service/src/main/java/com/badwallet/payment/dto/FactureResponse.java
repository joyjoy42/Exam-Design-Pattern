package com.badwallet.payment.dto;

import com.badwallet.payment.entity.Facture;
import com.badwallet.payment.entity.FactureStatus;
import com.badwallet.payment.entity.ServiceName;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FactureResponse(
        String reference,
        String walletCode,
        ServiceName serviceName,
        BigDecimal amount,
        BigDecimal amountDue,
        FactureStatus status,
        int periodMonth,
        int periodYear,
        LocalDate issuedAt
) {
    public static FactureResponse from(Facture facture) {
        return new FactureResponse(
                facture.getReference(),
                facture.getWalletCode(),
                facture.getServiceName(),
                facture.getAmount(),
                facture.getAmountDue(),
                facture.getStatus(),
                facture.getPeriodMonth(),
                facture.getPeriodYear(),
                facture.getIssuedAt());
    }
}
