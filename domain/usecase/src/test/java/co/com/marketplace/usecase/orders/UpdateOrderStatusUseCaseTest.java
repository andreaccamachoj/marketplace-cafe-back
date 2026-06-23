package co.com.marketplace.usecase.orders;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.User;
import co.com.marketplace.model.identity.gateways.UserGateway;
import co.com.marketplace.model.orders.Order;
import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.model.orders.OrderStatusChangedEvent;
import co.com.marketplace.model.orders.OrderStatusHistory;
import co.com.marketplace.model.orders.gateways.OrderEventPublisherGateway;
import co.com.marketplace.model.orders.gateways.OrderGateway;
import co.com.marketplace.model.orders.gateways.OrderStatusHistoryGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateOrderStatusUseCaseTest {

    @Mock private OrderGateway orderGateway;
    @Mock private OrderStatusHistoryGateway orderStatusHistoryGateway;
    @Mock private UserGateway userGateway;
    @Mock private OrderEventPublisherGateway orderEventPublisherGateway;

    @InjectMocks
    private UpdateOrderStatusUseCase useCase;

    private final UUID orderId = UUID.randomUUID();
    private final UUID producerId = UUID.randomUUID();
    private final UUID buyerId = UUID.randomUUID();

    private Order buildOrder() {
        return Order.builder().id(orderId).buyerId(buyerId).code("WCM-2026-001")
                .yearlySequence(1).year(2026).subtotal(BigDecimal.TEN)
                .shippingAmount(BigDecimal.ZERO).discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.valueOf(85000)).status(OrderStatus.confirmed)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
    }

    private OrderStatusHistory anyHistory() {
        return OrderStatusHistory.builder().id(UUID.randomUUID())
                .orderId(orderId).status(OrderStatus.preparing).changedBy(producerId)
                .changedAt(OffsetDateTime.now()).build();
    }

    @Test
    void execute_updatesStatusAndPublishesEvent_whenOrderFound() {
        Order order = buildOrder();
        Order updated = order.toBuilder().status(OrderStatus.preparing).build();
        User buyer = User.builder().id(buyerId).email("buyer@example.com").build();

        when(orderGateway.findById(orderId)).thenReturn(Mono.just(order));
        when(orderGateway.updateStatus(orderId, OrderStatus.preparing)).thenReturn(Mono.just(updated));
        when(orderStatusHistoryGateway.save(any())).thenReturn(Mono.just(anyHistory()));
        when(userGateway.findById(buyerId)).thenReturn(Mono.just(buyer));
        when(orderEventPublisherGateway.publishStatusChanged(any())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(orderId, producerId, OrderStatus.preparing, "Processing"))
                .expectNextMatches(o -> OrderStatus.preparing.equals(o.getStatus()))
                .verifyComplete();

        ArgumentCaptor<OrderStatusChangedEvent> captor = ArgumentCaptor.forClass(OrderStatusChangedEvent.class);
        verify(orderEventPublisherGateway).publishStatusChanged(captor.capture());
        OrderStatusChangedEvent event = captor.getValue();
        org.junit.jupiter.api.Assertions.assertEquals(orderId, event.orderId());
        org.junit.jupiter.api.Assertions.assertEquals("WCM-2026-001", event.orderCode());
        org.junit.jupiter.api.Assertions.assertEquals(OrderStatus.confirmed, event.previousStatus());
        org.junit.jupiter.api.Assertions.assertEquals(OrderStatus.preparing, event.newStatus());
        org.junit.jupiter.api.Assertions.assertEquals("buyer@example.com", event.buyerEmail());
        org.junit.jupiter.api.Assertions.assertEquals(BigDecimal.valueOf(85000), event.totalAmount());
        org.junit.jupiter.api.Assertions.assertEquals("Processing", event.note());
    }

    @Test
    void execute_stillSucceeds_whenPublishFails() {
        Order order = buildOrder();
        Order updated = order.toBuilder().status(OrderStatus.preparing).build();
        User buyer = User.builder().id(buyerId).email("buyer@example.com").build();

        when(orderGateway.findById(orderId)).thenReturn(Mono.just(order));
        when(orderGateway.updateStatus(orderId, OrderStatus.preparing)).thenReturn(Mono.just(updated));
        when(orderStatusHistoryGateway.save(any())).thenReturn(Mono.just(anyHistory()));
        when(userGateway.findById(buyerId)).thenReturn(Mono.just(buyer));
        when(orderEventPublisherGateway.publishStatusChanged(any()))
                .thenReturn(Mono.error(new RuntimeException("SQS unavailable")));

        StepVerifier.create(useCase.execute(orderId, producerId, OrderStatus.preparing, "Processing"))
                .expectNextMatches(o -> OrderStatus.preparing.equals(o.getStatus()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenOrderMissing() {
        when(orderGateway.findById(orderId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(orderId, producerId, OrderStatus.preparing, null))
                .verifyError(NotFoundException.class);

        verifyNoInteractions(orderEventPublisherGateway);
    }
}
