package com.badwallet.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record PayFacturesRequest(
        @NotBlank String phoneNumber,
        @NotBlank @Pattern(regexp = "ISM|WOYAFAL") String serviceName,
        @NotEmpty List<String> factureReferences
) {
}
