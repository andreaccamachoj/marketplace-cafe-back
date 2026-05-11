package co.com.marketplace.usecase.cart;

import co.com.marketplace.model.cart.Cart;
import co.com.marketplace.model.cart.gateways.CartGateway;
import reactor.core.publisher.Mono;

import java.util.UUID;

public final class RemoveCouponUseCase {

    private final CartGateway cartGateway;

    public RemoveCouponUseCase(CartGateway cartGateway) {
        this.cartGateway = cartGateway;
    }

    public Mono<Cart> execute(UUID userId) {
        return cartGateway.findByUserId(userId)
                .flatMap(cart -> cartGateway.removeCoupon(cart.getId())
                        .then(cartGateway.findByUserId(userId)));
    }
}
