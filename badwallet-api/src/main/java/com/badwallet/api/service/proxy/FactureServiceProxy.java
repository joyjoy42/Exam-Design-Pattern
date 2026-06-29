package com.badwallet.api.service.proxy;

import com.badwallet.api.dto.FactureDto;
import com.badwallet.api.dto.PaymentResultDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Proxy: the single gateway badwallet-api uses to reach payment-service over HTTP.
 * Every caller — the pass-through {@code /api/external/factures/...} endpoints and the
 * internal bill-payment flows — goes through this interface instead of holding its own
 * {@code RestClient}, so the base URL, request shaping and error translation live in
 * exactly one place.
 */
public interface FactureServiceProxy {

    List<FactureDto> getCurrentMonthFactures(String walletCode, String unite);

    List<FactureDto> getFacturesByPeriod(String walletCode, LocalDate debut, LocalDate fin);

    List<FactureDto> getFacturesByReferences(List<String> references);

    PaymentResultDto payCurrentMonth(String walletCode, String serviceName, BigDecimal amount);

    PaymentResultDto payByReferences(List<String> references);
}
