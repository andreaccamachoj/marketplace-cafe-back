package co.com.marketplace.usecase.orders;

import co.com.marketplace.model.orders.Order;
import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.model.orders.gateways.OrderGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListBuyerOrdersUseCaseTest {

    @Mock private OrderGateway orderGateway;

    @InjectMocks
    private ListBuyerOrdersUseCase useCase;

    private final UUID buyerId = UUID.randomUUID();

    @Test
    void execute_returnsOrders() {
        Order order = Order.builder().id(UUID.randomUUID()).buyerId(buyerId).code("WCM-2026-001")
                .yearlySequence(1).year(2026).subtotal(BigDecimal.TEN)
                .shippingAmount(BigDecimal.ZERO).discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.TEN).status(OrderStatus.pending_verification)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        when(orderGateway.findByBuyerId(buyerId, null, 0, 10)).thenReturn(Flux.just(order));

        StepVerifier.create(useCase.execute(buyerId, null, 0, 10))
                .expectNextMatches(o -> buyerId.equals(o.getBuyerId()))
                .verifyComplete();
    }

    @Test
    void execute_returnsEmpty_whenNoOrders() {
        when(orderGateway.findByBuyerId(buyerId, null, 0, 10)).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute(buyerId, null, 0, 10))
                .verifyComplete();
    }
}
