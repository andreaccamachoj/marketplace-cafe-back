package co.com.marketplace.usecase.cart;

import co.com.marketplace.model.cart.Cart;
import co.com.marketplace.model.cart.CartItem;
import co.com.marketplace.model.cart.gateways.CartGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public final class AddCartItemUseCase {

    private final CartGateway cartGateway;

    public record Command(
            UUID productId,
            int quantity,
            BigDecimal unitPriceSnapshot
    ) {}

    public Mono<Cart> execute(UUID userId, Command cmd) {
        return cartGateway.findByUserId(userId)
                .flatMap(cart -> {
                    CartItem item = CartItem.builder()
                            .cartId(cart.getId())
                            .productId(cmd.productId())
                            .quantity(cmd.quantity())
                            .unitPriceSnapshot(cmd.unitPriceSnapshot())
                            .addedAt(OffsetDateTime.now())
                            .build();
                    return cartGateway.saveItem(item);
                })
                .then(cartGateway.findByUserId(userId));
    }
}
