package com.badwallet.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record WithdrawRequest(
        @NotBlank String phoneNumber,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount
) {
}
