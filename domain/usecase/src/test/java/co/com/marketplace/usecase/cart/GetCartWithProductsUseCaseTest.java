package co.com.marketplace.usecase.cart;

import co.com.marketplace.model.cart.Cart;
import co.com.marketplace.model.cart.CartItem;
import co.com.marketplace.model.cart.gateways.CartGateway;
import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.ProductStatus;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCartWithProductsUseCaseTest {

    @Mock private CartGateway cartGateway;
    @Mock private ProductGateway productGateway;

    @InjectMocks
    private GetCartWithProductsUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID cartId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @Test
    void execute_returnsCartDetail_withItems() {
        CartItem item = CartItem.builder().id(UUID.randomUUID()).cartId(cartId)
                .productId(productId).quantity(2).unitPriceSnapshot(BigDecimal.TEN)
                .addedAt(OffsetDateTime.now()).build();
        Cart cart = Cart.builder().id(cartId).userId(userId).items(List.of(item))
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        Product product = Product.builder().id(productId).name("Café").price(BigDecimal.TEN)
                .status(ProductStatus.active).soldCount(0).emoji("☕")
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();

        when(cartGateway.findByUserId(userId)).thenReturn(Mono.just(cart));
        when(productGateway.findById(productId)).thenReturn(Mono.just(product));

        StepVerifier.create(useCase.execute(userId))
                .expectNextMatches(d -> d.items().size() == 1 && "Café".equals(d.items().get(0).productName()))
                .verifyComplete();
    }

    @Test
    void execute_returnsEmptyItems_whenCartEmpty() {
        Cart cart = Cart.builder().id(cartId).userId(userId).items(List.of())
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();

        when(cartGateway.findByUserId(userId)).thenReturn(Mono.just(cart));

        StepVerifier.create(useCase.execute(userId))
                .expectNextMatches(d -> d.items().isEmpty())
                .verifyComplete();
    }
}
