package co.com.marketplace.model.cart.gateways;

import co.com.marketplace.model.cart.Cart;
import co.com.marketplace.model.cart.CartItem;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CartGateway {
    Mono<Cart> findByUserId(UUID userId);
    Mono<Cart> save(Cart cart);
    Mono<CartItem> saveItem(CartItem item);
    Mono<CartItem> updateItemQuantity(UUID itemId, int quantity);
    Mono<Void> deleteItem(UUID itemId);
    Mono<Void> clearItems(UUID cartId);
    Mono<Void> applyCoupon(UUID cartId, Integer couponId);
    Mono<Void> removeCoupon(UUID cartId);
    Mono<Void> applyShipping(UUID cartId, String shippingOptionId);
}
