package co.com.marketplace.usecase.cart;

import co.com.marketplace.model.cart.Cart;
import co.com.marketplace.model.cart.gateways.CartGateway;
import reactor.core.publisher.Mono;

import java.util.UUID;

public final class GetCartUseCase {

    private final CartGateway cartGateway;

    public GetCartUseCase(CartGateway cartGateway) {
        this.cartGateway = cartGateway;
    }

    public Mono<Cart> execute(UUID userId) {
        return cartGateway.findByUserId(userId);
    }
}
