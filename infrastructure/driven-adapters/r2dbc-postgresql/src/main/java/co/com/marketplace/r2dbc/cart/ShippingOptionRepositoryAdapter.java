package co.com.marketplace.r2dbc.cart;

import co.com.marketplace.model.cart.ShippingOption;
import co.com.marketplace.model.cart.gateways.ShippingOptionGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShippingOptionRepositoryAdapter implements ShippingOptionGateway {

    private final ShippingOptionReactiveRepository repo;

    @Override
    public Flux<ShippingOption> findAll() {
        return repo.findAll()
                .doOnSubscribe(s -> log.debug("[ShippingOptionRepositoryAdapter#findAll] DB request"))
                .doOnComplete(() -> log.debug("[ShippingOptionRepositoryAdapter#findAll] DB response: complete"))
                .doOnError(e -> log.error("[ShippingOptionRepositoryAdapter#findAll] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    @Override
    public Mono<ShippingOption> findById(String id) {
        return repo.findById(id)
                .doOnSubscribe(s -> log.debug("[ShippingOptionRepositoryAdapter#findById] DB request: id={}", id))
                .doOnSuccess(r -> log.debug("[ShippingOptionRepositoryAdapter#findById] DB response: found={}", r != null))
                .doOnError(e -> log.error("[ShippingOptionRepositoryAdapter#findById] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    private ShippingOption toDomain(ShippingOptionData d) {
        return ShippingOption.builder()
                .id(d.getId())
                .name(d.getName())
                .deliveryWindow(d.getDeliveryWindow())
                .price(d.getPrice())
                .isActive(d.isActive())
                .displayOrder(d.getDisplayOrder())
                .build();
    }
}
