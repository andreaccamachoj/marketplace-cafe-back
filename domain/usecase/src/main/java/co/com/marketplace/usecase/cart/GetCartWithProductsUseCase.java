package co.com.marketplace.usecase.cart;

import co.com.marketplace.model.cart.Cart;
import co.com.marketplace.model.cart.CartItem;
import co.com.marketplace.model.cart.gateways.CartGateway;
import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public final class GetCartWithProductsUseCase {

    private final CartGateway cartGateway;
    private final ProductGateway productGateway;

    public record CartItemDetail(UUID id, UUID productId, String productName,
                                 String producerName, BigDecimal price, int quantity,
                                 String emoji, int maxStock) {}

    public record CartDetail(UUID id, List<CartItemDetail> items,
                             String couponCode, String shippingOptionId) {}

    public Mono<CartDetail> execute(UUID userId) {
        return cartGateway.findByUserId(userId)
                .flatMap(cart -> enrichItems(cart));
    }

    private Mono<CartDetail> enrichItems(Cart cart) {
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            return Mono.just(new CartDetail(cart.getId(), List.of(), null, cart.getShippingOptionId()));
        }
        return Flux.fromIterable(cart.getItems())
                .flatMap(item -> productGateway.findById(item.getProductId())
                        .map(p -> toDetail(item, p))
                        .onErrorReturn(toDetailNoProduct(item)))
                .collectList()
                .map(details -> new CartDetail(cart.getId(), details, null, cart.getShippingOptionId()));
    }

    private CartItemDetail toDetail(CartItem item, Product p) {
        return new CartItemDetail(item.getId(), item.getProductId(),
                p.getName(), p.getProducerName() != null ? p.getProducerName() : "",
                item.getUnitPriceSnapshot(), item.getQuantity(),
                p.getEmoji() != null ? p.getEmoji() : "☕", 99);
    }

    private CartItemDetail toDetailNoProduct(CartItem item) {
        return new CartItemDetail(item.getId(), item.getProductId(),
                "Producto", "", item.getUnitPriceSnapshot(), item.getQuantity(), "☕", 99);
    }
}
