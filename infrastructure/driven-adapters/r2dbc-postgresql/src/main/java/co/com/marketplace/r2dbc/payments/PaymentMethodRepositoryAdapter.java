package co.com.marketplace.r2dbc.payments;

import co.com.marketplace.model.payments.PaymentMethod;
import co.com.marketplace.model.payments.gateways.PaymentMethodGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentMethodRepositoryAdapter implements PaymentMethodGateway {

    private final PaymentMethodReactiveRepository repo;

    @Override
    public Flux<PaymentMethod> findAllActive() {
        return repo.findAllActive()
                .doOnSubscribe(s -> log.debug("[PaymentMethodRepositoryAdapter#findAllActive] DB request"))
                .doOnComplete(() -> log.debug("[PaymentMethodRepositoryAdapter#findAllActive] DB response: complete"))
                .doOnError(e -> log.error("[PaymentMethodRepositoryAdapter#findAllActive] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    @Override
    public Mono<PaymentMethod> findById(UUID id) {
        return repo.findById(id)
                .doOnSubscribe(s -> log.debug("[PaymentMethodRepositoryAdapter#findById] DB request: id={}", id))
                .doOnSuccess(r -> log.debug("[PaymentMethodRepositoryAdapter#findById] DB response: found={}", r != null))
                .doOnError(e -> log.error("[PaymentMethodRepositoryAdapter#findById] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    @Override
    public Mono<PaymentMethod> findByCode(String code) {
        return repo.findByCode(code)
                .doOnSubscribe(s -> log.debug("[PaymentMethodRepositoryAdapter#findByCode] DB request: code={}", code))
                .doOnSuccess(r -> log.debug("[PaymentMethodRepositoryAdapter#findByCode] DB response: found={}", r != null))
                .doOnError(e -> log.error("[PaymentMethodRepositoryAdapter#findByCode] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    private PaymentMethod toDomain(PaymentMethodData d) {
        return PaymentMethod.builder()
                .id(d.getId())
                .code(d.getCode())
                .name(d.getName())
                .type(d.getType())
                .accountNumber(d.getAccountNumber())
                .accountHolder(d.getAccountHolder())
                .bank(d.getBank())
                .alias(d.getAlias())
                .nit(d.getNit())
                .emoji(d.getEmoji())
                .accentColor(d.getAccentColor())
                .isActive(d.isActive())
                .displayOrder(d.getDisplayOrder())
                .build();
    }
}
