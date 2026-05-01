package co.com.marketplace.model.cart.gateways;

import co.com.marketplace.model.cart.ShippingOption;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ShippingOptionGateway {
    Flux<ShippingOption> findAll();
    Mono<ShippingOption> findById(String id);
}
