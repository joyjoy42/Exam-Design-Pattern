package com.badwallet.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record PayRequest(
        @NotBlank String phoneNumber,
        @NotBlank @Pattern(regexp = "ISM|WOYAFAL") String serviceName,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount
) {
}
