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
class RemoveCouponUseCaseTest {

    @Mock private CartGateway cartGateway;

    @InjectMocks
    private RemoveCouponUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID cartId = UUID.randomUUID();

    @Test
    void execute_removesCouponAndReturnsCart() {
        Cart cart = Cart.builder().id(cartId).userId(userId).couponId(1)
                .items(List.of()).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        Cart updated = cart.toBuilder().couponId(null).build();

        when(cartGateway.findByUserId(userId)).thenReturn(Mono.just(cart)).thenReturn(Mono.just(updated));
        when(cartGateway.removeCoupon(cartId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(userId))
                .expectNextMatches(c -> c.getCouponId() == null)
                .verifyComplete();
    }
}
