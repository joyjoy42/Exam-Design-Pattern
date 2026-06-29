package com.badwallet.payment.dto;

import com.badwallet.payment.entity.ServiceName;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PayCurrentMonthRequest(
        @NotNull String walletCode,
        @NotNull ServiceName serviceName,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount
) {
}
