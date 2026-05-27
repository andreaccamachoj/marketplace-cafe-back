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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddCartItemUseCaseTest {

    @Mock private CartGateway cartGateway;

    @InjectMocks
    private AddCartItemUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID cartId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @Test
    void execute_addsItemAndReturnsUpdatedCart() {
        Cart cart = Cart.builder().id(cartId).userId(userId)
                .items(List.of()).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        CartItem savedItem = CartItem.builder().id(UUID.randomUUID()).cartId(cartId)
                .productId(productId).quantity(2).unitPriceSnapshot(BigDecimal.TEN)
                .addedAt(OffsetDateTime.now()).build();
        Cart updatedCart = cart.toBuilder().items(List.of(savedItem)).build();

        when(cartGateway.findByUserId(userId)).thenReturn(Mono.just(cart)).thenReturn(Mono.just(updatedCart));
        when(cartGateway.saveItem(any())).thenReturn(Mono.just(savedItem));

        AddCartItemUseCase.Command cmd = new AddCartItemUseCase.Command(productId, 2, BigDecimal.TEN);

        StepVerifier.create(useCase.execute(userId, cmd))
                .expectNextMatches(c -> c.getItems() != null && c.getItems().size() == 1)
                .verifyComplete();
    }
}
