package com.badwallet.api.service.proxy;

import com.badwallet.api.dto.FactureDto;
import com.badwallet.api.dto.PaymentResultDto;
import com.badwallet.api.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class FactureServiceProxyImpl implements FactureServiceProxy {

    private final RestClient paymentServiceRestClient;

    @Override
    public List<FactureDto> getCurrentMonthFactures(String walletCode, String unite) {
        return call(() -> paymentServiceRestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/factures/{walletCode}/current")
                        .queryParamIfPresent("unite", java.util.Optional.ofNullable(unite))
                        .build(walletCode))
                .retrieve()
                .body(new ParameterizedTypeReference<List<FactureDto>>() {
                }));
    }

    @Override
    public List<FactureDto> getFacturesByPeriod(String walletCode, LocalDate debut, LocalDate fin) {
        return call(() -> paymentServiceRestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/factures/{walletCode}/periode")
                        .queryParam("debut", debut)
                        .queryParam("fin", fin)
                        .build(walletCode))
                .retrieve()
                .body(new ParameterizedTypeReference<List<FactureDto>>() {
                }));
    }

    @Override
    public List<FactureDto> getFacturesByReferences(List<String> references) {
        return call(() -> paymentServiceRestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/factures/by-references")
                        .queryParam("refs", references)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<FactureDto>>() {
                }));
    }

    @Override
    public PaymentResultDto payCurrentMonth(String walletCode, String serviceName, BigDecimal amount) {
        return call(() -> paymentServiceRestClient.post()
                .uri("/api/factures/pay")
                .body(Map.of("walletCode", walletCode, "serviceName", serviceName, "amount", amount))
                .retrieve()
                .body(PaymentResultDto.class));
    }

    @Override
    public PaymentResultDto payByReferences(List<String> references) {
        return call(() -> paymentServiceRestClient.post()
                .uri("/api/factures/pay-by-references")
                .body(Map.of("factureReferences", references))
                .retrieve()
                .body(PaymentResultDto.class));
    }

    @Override
    public void seedFactures(int numWallets, int monthsOfHistory) {
        call(() -> paymentServiceRestClient.post()
                .uri(uriBuilder -> uriBuilder.path("/api/factures/seed")
                        .queryParam("numWallets", numWallets)
                        .queryParam("monthsOfHistory", monthsOfHistory)
                        .build())
                .retrieve()
                .body(Map.class));
    }

    private <T> T call(java.util.function.Supplier<T> request) {
        try {
            return request.get();
        } catch (RestClientException ex) {
            throw new ExternalServiceException("payment-service indisponible ou en erreur: " + ex.getMessage(), ex);
        }
    }
}
