package co.com.marketplace.usecase.cart;

import co.com.marketplace.model.cart.Cart;
import co.com.marketplace.model.cart.gateways.CartGateway;
import co.com.marketplace.model.cart.gateways.ShippingOptionGateway;
import co.com.marketplace.model.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public final class SelectShippingOptionUseCase {

    private final CartGateway cartGateway;
    private final ShippingOptionGateway shippingOptionGateway;

    public Mono<Cart> execute(UUID userId, String shippingOptionId) {
        return shippingOptionGateway.findById(shippingOptionId)
                .switchIfEmpty(Mono.error(new NotFoundException("SHIPPING_OPTION_NOT_FOUND", "Shipping option not found: " + shippingOptionId)))
                .flatMap(option -> cartGateway.findByUserId(userId)
                        .flatMap(cart -> cartGateway.applyShipping(cart.getId(), shippingOptionId)
                                .then(cartGateway.findByUserId(userId))));
    }
}
