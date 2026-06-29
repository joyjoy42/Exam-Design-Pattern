package com.badwallet.api.service.strategy;

import com.badwallet.api.entity.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Factory: resolves the right {@link PaymentMethodStrategy} bean for a given {@link PaymentMethod}. */
@Component
public class PaymentStrategyFactory {

    private final Map<PaymentMethod, PaymentMethodStrategy> strategiesByMethod;

    public PaymentStrategyFactory(List<PaymentMethodStrategy> strategies) {
        this.strategiesByMethod = strategies.stream()
                .collect(Collectors.toMap(PaymentMethodStrategy::supportedMethod, Function.identity()));
    }

    public PaymentMethodStrategy get(PaymentMethod method) {
        PaymentMethodStrategy strategy = strategiesByMethod.get(method);
        if (strategy == null) {
            throw new IllegalArgumentException("Méthode de paiement non supportée: " + method);
        }
        return strategy;
    }
}
