package co.com.marketplace.usecase.cart;

import co.com.marketplace.model.cart.Cart;
import co.com.marketplace.model.cart.ShippingOption;
import co.com.marketplace.model.cart.gateways.CartGateway;
import co.com.marketplace.model.cart.gateways.ShippingOptionGateway;
import co.com.marketplace.model.exception.NotFoundException;
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
class SelectShippingOptionUseCaseTest {

    @Mock private CartGateway cartGateway;
    @Mock private ShippingOptionGateway shippingOptionGateway;

    @InjectMocks
    private SelectShippingOptionUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID cartId = UUID.randomUUID();

    @Test
    void execute_appliesShippingAndReturnsCart() {
        ShippingOption option = ShippingOption.builder().id("std").name("Standard")
                .price(BigDecimal.valueOf(5)).isActive(true).displayOrder(1).build();
        Cart cart = Cart.builder().id(cartId).userId(userId).items(List.of())
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        Cart updated = cart.toBuilder().shippingOptionId("std").build();

        when(shippingOptionGateway.findById("std")).thenReturn(Mono.just(option));
        when(cartGateway.findByUserId(userId)).thenReturn(Mono.just(cart)).thenReturn(Mono.just(updated));
        when(cartGateway.applyShipping(cartId, "std")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(userId, "std"))
                .expectNextMatches(c -> "std".equals(c.getShippingOptionId()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenShippingOptionMissing() {
        when(shippingOptionGateway.findById("unknown")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(userId, "unknown"))
                .verifyError(NotFoundException.class);
    }
}
