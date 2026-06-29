package com.badwallet.payment.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record PayByReferencesRequest(
        @NotEmpty List<String> factureReferences
) {
}
