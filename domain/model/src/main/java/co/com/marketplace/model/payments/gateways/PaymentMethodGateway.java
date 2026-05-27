package co.com.marketplace.model.payments.gateways;

import co.com.marketplace.model.payments.PaymentMethod;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PaymentMethodGateway {
    Flux<PaymentMethod> findAllActive();
    Mono<PaymentMethod> findById(UUID id);
    Mono<PaymentMethod> findByCode(String code);
}
