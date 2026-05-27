package co.com.marketplace.r2dbc.cart;

import co.com.marketplace.model.cart.*;
import co.com.marketplace.model.cart.gateways.CartGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CartRepositoryAdapter implements CartGateway {

    private final CartReactiveRepository cartRepo;
    private final CartItemReactiveRepository cartItemRepo;
    private final R2dbcEntityTemplate template;
    private final DatabaseClient db;

    @Override
    public Mono<Cart> findByUserId(UUID userId) {
        return cartRepo.findByUserId(userId)
                .doOnSubscribe(s -> log.debug("[CartRepositoryAdapter#findByUserId] DB request: userId={}", userId))
                .doOnSuccess(r -> log.debug("[CartRepositoryAdapter#findByUserId] DB response: found={}", r != null))
                .doOnError(e -> log.error("[CartRepositoryAdapter#findByUserId] DB error: {}", e.getMessage()))
                .flatMap(cartData -> cartItemRepo.findByCartId(cartData.getId())
                        .map(this::toItemDomain)
                        .collectList()
                        .map(items -> toCartDomain(cartData, items)));
    }

    @Override
    public Mono<Cart> save(Cart cart) {
        CartData data = toCartData(cart);
        return cartRepo.save(data)
                .doOnSubscribe(s -> log.debug("[CartRepositoryAdapter#save] DB request: userId={}", cart.getUserId()))
                .doOnSuccess(r -> log.debug("[CartRepositoryAdapter#save] DB response: result={}", r != null))
                .doOnError(e -> log.error("[CartRepositoryAdapter#save] DB error: {}", e.getMessage()))
                .map(saved -> toCartDomain(saved, cart.getItems() != null ? cart.getItems() : Collections.emptyList()));
    }

    @Override
    public Mono<CartItem> saveItem(CartItem item) {
        return db.sql(
                "INSERT INTO marketplace.cart_items (cart_id, product_id, quantity, unit_price_snapshot, added_at) " +
                "VALUES (:cartId, :productId, :quantity, :unitPriceSnapshot, :addedAt) " +
                "ON CONFLICT (cart_id, product_id) DO UPDATE " +
                "SET quantity = cart_items.quantity + EXCLUDED.quantity, " +
                "    unit_price_snapshot = EXCLUDED.unit_price_snapshot " +
                "RETURNING *")
                .bind("cartId", item.getCartId())
                .bind("productId", item.getProductId())
                .bind("quantity", item.getQuantity())
                .bind("unitPriceSnapshot", item.getUnitPriceSnapshot())
                .bind("addedAt", item.getAddedAt() != null ? item.getAddedAt() : java.time.OffsetDateTime.now())
                .map((row, meta) -> CartItemData.builder()
                        .id(row.get("id", UUID.class))
                        .cartId(row.get("cart_id", UUID.class))
                        .productId(row.get("product_id", UUID.class))
                        .quantity(row.get("quantity", Integer.class))
                        .unitPriceSnapshot(row.get("unit_price_snapshot", java.math.BigDecimal.class))
                        .addedAt(row.get("added_at", java.time.OffsetDateTime.class))
                        .build()).one()
                .doOnSubscribe(s -> log.debug("[CartRepositoryAdapter#saveItem] DB request: cartId={}", item.getCartId()))
                .doOnSuccess(r -> log.debug("[CartRepositoryAdapter#saveItem] DB response: result={}", r != null))
                .doOnError(e -> log.error("[CartRepositoryAdapter#saveItem] DB error [{}]: {}", e.getClass().getSimpleName(), e.getMessage()))
                .map(this::toItemDomain);
    }

    @Override
    public Mono<CartItem> updateItemQuantity(UUID itemId, int quantity) {
        return db.sql("UPDATE marketplace.cart_items SET quantity = :qty WHERE id = :id RETURNING *")
                .bind("qty", quantity)
                .bind("id", itemId)
                .map((row, meta) -> CartItemData.builder()
                        .id(row.get("id", UUID.class))
                        .cartId(row.get("cart_id", UUID.class))
                        .productId(row.get("product_id", UUID.class))
                        .quantity(row.get("quantity", Integer.class))
                        .unitPriceSnapshot(row.get("unit_price_snapshot", java.math.BigDecimal.class))
                        .addedAt(row.get("added_at", java.time.OffsetDateTime.class))
                        .build())
                .one()
                .doOnSubscribe(s -> log.debug("[CartRepositoryAdapter#updateItemQuantity] DB request: itemId={}, quantity={}", itemId, quantity))
                .doOnSuccess(r -> log.debug("[CartRepositoryAdapter#updateItemQuantity] DB response: result={}", r != null))
                .doOnError(e -> log.error("[CartRepositoryAdapter#updateItemQuantity] DB error: {}", e.getMessage()))
                .map(this::toItemDomain);
    }

    @Override
    public Mono<Void> deleteItem(UUID itemId) {
        return cartItemRepo.deleteById(itemId)
                .doOnSubscribe(s -> log.debug("[CartRepositoryAdapter#deleteItem] DB request: itemId={}", itemId))
                .doOnTerminate(() -> log.debug("[CartRepositoryAdapter#deleteItem] DB response: done"))
                .doOnError(e -> log.error("[CartRepositoryAdapter#deleteItem] DB error: {}", e.getMessage()));
    }

    @Override
    public Mono<Void> clearItems(UUID cartId) {
        return cartItemRepo.deleteByCartId(cartId)
                .doOnSubscribe(s -> log.debug("[CartRepositoryAdapter#clearItems] DB request: cartId={}", cartId))
                .doOnTerminate(() -> log.debug("[CartRepositoryAdapter#clearItems] DB response: done"))
                .doOnError(e -> log.error("[CartRepositoryAdapter#clearItems] DB error: {}", e.getMessage()));
    }

    @Override
    public Mono<Void> applyCoupon(UUID cartId, Integer couponId) {
        return db.sql("UPDATE marketplace.carts SET coupon_id = :couponId WHERE id = :id")
                .bind("couponId", couponId)
                .bind("id", cartId)
                .then()
                .doOnSubscribe(s -> log.debug("[CartRepositoryAdapter#applyCoupon] DB request: cartId={}, couponId={}", cartId, couponId))
                .doOnTerminate(() -> log.debug("[CartRepositoryAdapter#applyCoupon] DB response: done"))
                .doOnError(e -> log.error("[CartRepositoryAdapter#applyCoupon] DB error: {}", e.getMessage()));
    }

    @Override
    public Mono<Void> removeCoupon(UUID cartId) {
        return db.sql("UPDATE marketplace.carts SET coupon_id = NULL WHERE id = :id")
                .bind("id", cartId)
                .then()
                .doOnSubscribe(s -> log.debug("[CartRepositoryAdapter#removeCoupon] DB request: cartId={}", cartId))
                .doOnTerminate(() -> log.debug("[CartRepositoryAdapter#removeCoupon] DB response: done"))
                .doOnError(e -> log.error("[CartRepositoryAdapter#removeCoupon] DB error: {}", e.getMessage()));
    }

    @Override
    public Mono<Void> applyShipping(UUID cartId, String shippingOptionId) {
        return db.sql("UPDATE marketplace.carts SET shipping_option_id = :shippingOptionId WHERE id = :id")
                .bind("shippingOptionId", shippingOptionId)
                .bind("id", cartId)
                .then()
                .doOnSubscribe(s -> log.debug("[CartRepositoryAdapter#applyShipping] DB request: cartId={}, shippingOptionId={}", cartId, shippingOptionId))
                .doOnTerminate(() -> log.debug("[CartRepositoryAdapter#applyShipping] DB response: done"))
                .doOnError(e -> log.error("[CartRepositoryAdapter#applyShipping] DB error: {}", e.getMessage()));
    }

    private Cart toCartDomain(CartData d, java.util.List<CartItem> items) {
        return Cart.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .couponId(d.getCouponId())
                .shippingOptionId(d.getShippingOptionId())
                .items(items)
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    private CartData toCartData(Cart c) {
        return CartData.builder()
                .id(c.getId())
                .userId(c.getUserId())
                .couponId(c.getCouponId())
                .shippingOptionId(c.getShippingOptionId())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }

    private CartItem toItemDomain(CartItemData d) {
        return CartItem.builder()
                .id(d.getId())
                .cartId(d.getCartId())
                .productId(d.getProductId())
                .quantity(d.getQuantity())
                .unitPriceSnapshot(d.getUnitPriceSnapshot())
                .addedAt(d.getAddedAt())
                .build();
    }

    private CartItemData toItemData(CartItem i) {
        return CartItemData.builder()
                .id(i.getId())
                .cartId(i.getCartId())
                .productId(i.getProductId())
                .quantity(i.getQuantity())
                .unitPriceSnapshot(i.getUnitPriceSnapshot())
                .addedAt(i.getAddedAt())
                .build();
    }
}
