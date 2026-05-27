package co.com.marketplace.usecase.cart;

import co.com.marketplace.model.cart.Cart;
import co.com.marketplace.model.cart.CartItem;
import co.com.marketplace.model.cart.gateways.CartGateway;
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
class UpdateCartItemQuantityUseCaseTest {

    @Mock private CartGateway cartGateway;

    @InjectMocks
    private UpdateCartItemQuantityUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID itemId = UUID.randomUUID();

    @Test
    void execute_updatesQuantityAndReturnsCart() {
        CartItem item = CartItem.builder().id(itemId).cartId(UUID.randomUUID())
                .productId(UUID.randomUUID()).quantity(5).unitPriceSnapshot(BigDecimal.TEN)
                .addedAt(OffsetDateTime.now()).build();
        Cart cart = Cart.builder().id(UUID.randomUUID()).userId(userId)
                .items(List.of(item)).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();

        when(cartGateway.updateItemQuantity(itemId, 5)).thenReturn(Mono.just(item));
        when(cartGateway.findByUserId(userId)).thenReturn(Mono.just(cart));

        StepVerifier.create(useCase.execute(userId, itemId, 5))
                .expectNextMatches(c -> userId.equals(c.getUserId()))
                .verifyComplete();
    }
}
