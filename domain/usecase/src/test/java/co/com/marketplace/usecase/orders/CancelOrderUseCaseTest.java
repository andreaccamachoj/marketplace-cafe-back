package co.com.marketplace.usecase.orders;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.exception.ValidationException;
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
class CancelOrderUseCaseTest {

    @Mock private OrderGateway orderGateway;
    @Mock private OrderStatusHistoryGateway orderStatusHistoryGateway;

    @InjectMocks
    private CancelOrderUseCase useCase;

    private final UUID orderId = UUID.randomUUID();
    private final UUID buyerId = UUID.randomUUID();

    private Order buildOrder(OrderStatus status) {
        return Order.builder().id(orderId).buyerId(buyerId).code("WCM-2026-001")
                .yearlySequence(1).year(2026).subtotal(BigDecimal.TEN)
                .shippingAmount(BigDecimal.ZERO).discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.TEN).status(status)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
    }

    @Test
    void execute_cancelsOrder_whenPendingVerification() {
        Order order = buildOrder(OrderStatus.pending_verification);
        Order cancelled = order.toBuilder().status(OrderStatus.cancelled).build();
        OrderStatusHistory history = OrderStatusHistory.builder().id(UUID.randomUUID())
                .orderId(orderId).status(OrderStatus.cancelled).changedBy(buyerId)
                .changedAt(OffsetDateTime.now()).build();

        when(orderGateway.findById(orderId)).thenReturn(Mono.just(order));
        when(orderGateway.updateStatus(orderId, OrderStatus.cancelled)).thenReturn(Mono.just(cancelled));
        when(orderStatusHistoryGateway.save(any())).thenReturn(Mono.just(history));

        StepVerifier.create(useCase.execute(orderId, buyerId, "Changed mind"))
                .expectNextMatches(o -> OrderStatus.cancelled.equals(o.getStatus()))
                .verifyComplete();
    }

    @Test
    void execute_throwsValidation_whenOrderShipped() {
        Order order = buildOrder(OrderStatus.shipped);
        when(orderGateway.findById(orderId)).thenReturn(Mono.just(order));

        StepVerifier.create(useCase.execute(orderId, buyerId, "reason"))
                .verifyError(ValidationException.class);
    }

    @Test
    void execute_throwsNotFound_whenOrderMissing() {
        when(orderGateway.findById(orderId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(orderId, buyerId, "reason"))
                .verifyError(NotFoundException.class);
    }
}
