package co.com.marketplace.usecase.cart;

import co.com.marketplace.model.cart.Cart;
import co.com.marketplace.model.cart.gateways.CartGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public final class UpdateCartItemQuantityUseCase {

    private final CartGateway cartGateway;

    public Mono<Cart> execute(UUID userId, UUID itemId, int quantity) {
        return cartGateway.updateItemQuantity(itemId, quantity)
                .then(cartGateway.findByUserId(userId));
    }
}
