package co.com.marketplace.usecase.cart;

import co.com.marketplace.model.cart.Cart;
import co.com.marketplace.model.cart.gateways.CartGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCartUseCaseTest {

    @Mock private CartGateway cartGateway;

    @InjectMocks
    private GetCartUseCase useCase;

    private final UUID userId = UUID.randomUUID();

    @Test
    void execute_returnsCart_whenFound() {
        Cart cart = Cart.builder().id(UUID.randomUUID()).userId(userId)
                .items(List.of()).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        when(cartGateway.findByUserId(userId)).thenReturn(Mono.just(cart));

        StepVerifier.create(useCase.execute(userId))
                .expectNextMatches(c -> userId.equals(c.getUserId()))
                .verifyComplete();
    }

    @Test
    void execute_returnsEmpty_whenCartMissing() {
        when(cartGateway.findByUserId(userId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(userId))
                .verifyComplete();
    }
}
