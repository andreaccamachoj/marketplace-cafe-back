package co.com.marketplace.usecase.orders;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.orders.Order;
import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.model.orders.gateways.OrderGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetOrderDetailUseCaseTest {

    @Mock private OrderGateway orderGateway;

    @InjectMocks
    private GetOrderDetailUseCase useCase;

    private final UUID orderId = UUID.randomUUID();

    @Test
    void execute_returnsOrder_whenFound() {
        Order order = Order.builder().id(orderId).buyerId(UUID.randomUUID()).code("WCM-2026-001")
                .yearlySequence(1).year(2026).subtotal(BigDecimal.TEN)
                .shippingAmount(BigDecimal.ZERO).discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.TEN).status(OrderStatus.confirmed)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        when(orderGateway.findById(orderId)).thenReturn(Mono.just(order));

        StepVerifier.create(useCase.execute(orderId))
                .expectNextMatches(o -> orderId.equals(o.getId()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenMissing() {
        when(orderGateway.findById(orderId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(orderId))
                .verifyError(NotFoundException.class);
    }
}
