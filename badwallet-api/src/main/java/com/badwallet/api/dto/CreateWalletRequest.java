package com.badwallet.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record CreateWalletRequest(
        @NotBlank @Pattern(regexp = "\\+?[0-9]{8,15}") String phoneNumber,
        @NotBlank @Email String email,
        @NotNull @DecimalMin(value = "0", inclusive = true) BigDecimal initialBalance,
        @NotBlank String code,
        @NotBlank String currency
) {
}
