package co.com.marketplace.usecase.cart;

import co.com.marketplace.model.cart.gateways.CartGateway;
import reactor.core.publisher.Mono;

import java.util.UUID;

public final class ClearCartUseCase {

    private final CartGateway cartGateway;

    public ClearCartUseCase(CartGateway cartGateway) {
        this.cartGateway = cartGateway;
    }

    public Mono<Void> execute(UUID userId) {
        return cartGateway.findByUserId(userId)
                .flatMap(cart -> cartGateway.clearItems(cart.getId()));
    }
}
