package co.com.marketplace.usecase.orders;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.orders.Order;
import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.model.orders.OrderStatusHistory;
import co.com.marketplace.model.orders.gateways.OrderGateway;
import co.com.marketplace.model.orders.gateways.OrderStatusHistoryGateway;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateOrderStatusUseCaseTest {

    @Mock private OrderGateway orderGateway;
    @Mock private OrderStatusHistoryGateway orderStatusHistoryGateway;

    @InjectMocks
    private UpdateOrderStatusUseCase useCase;

    private final UUID orderId = UUID.randomUUID();
    private final UUID producerId = UUID.randomUUID();

    @Test
    void execute_updatesStatus_whenOrderFound() {
        Order order = Order.builder().id(orderId).buyerId(UUID.randomUUID()).code("WCM-2026-001")
                .yearlySequence(1).year(2026).subtotal(BigDecimal.TEN)
                .shippingAmount(BigDecimal.ZERO).discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.TEN).status(OrderStatus.confirmed)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        Order updated = order.toBuilder().status(OrderStatus.preparing).build();
        OrderStatusHistory history = OrderStatusHistory.builder().id(UUID.randomUUID())
                .orderId(orderId).status(OrderStatus.preparing).changedBy(producerId)
                .changedAt(OffsetDateTime.now()).build();

        when(orderGateway.findById(orderId)).thenReturn(Mono.just(order));
        when(orderGateway.updateStatus(orderId, OrderStatus.preparing)).thenReturn(Mono.just(updated));
        when(orderStatusHistoryGateway.save(any())).thenReturn(Mono.just(history));

        StepVerifier.create(useCase.execute(orderId, producerId, OrderStatus.preparing, "Processing"))
                .expectNextMatches(o -> OrderStatus.preparing.equals(o.getStatus()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenOrderMissing() {
        when(orderGateway.findById(orderId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(orderId, producerId, OrderStatus.preparing, null))
                .verifyError(NotFoundException.class);
    }
}
