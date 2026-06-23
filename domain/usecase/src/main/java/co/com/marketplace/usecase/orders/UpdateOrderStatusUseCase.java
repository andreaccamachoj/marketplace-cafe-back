package co.com.marketplace.usecase.orders;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.gateways.UserGateway;
import co.com.marketplace.model.orders.Order;
import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.model.orders.OrderStatusChangedEvent;
import co.com.marketplace.model.orders.OrderStatusHistory;
import co.com.marketplace.model.orders.gateways.OrderEventPublisherGateway;
import co.com.marketplace.model.orders.gateways.OrderGateway;
import co.com.marketplace.model.orders.gateways.OrderStatusHistoryGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public final class UpdateOrderStatusUseCase {

    private final OrderGateway orderGateway;
    private final OrderStatusHistoryGateway orderStatusHistoryGateway;
    private final UserGateway userGateway;
    private final OrderEventPublisherGateway orderEventPublisherGateway;

    public Mono<Order> execute(UUID orderId, UUID producerId, OrderStatus newStatus, String note) {
        return orderGateway.findById(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("ORDER_NOT_FOUND", "Order not found: " + orderId)))
                .flatMap(order -> {
                    OrderStatus previousStatus = order.getStatus();
                    return orderGateway.updateStatus(orderId, newStatus)
                            .flatMap(updated -> {
                                OrderStatusHistory history = OrderStatusHistory.builder()
                                        .id(UUID.randomUUID())
                                        .orderId(orderId)
                                        .status(newStatus)
                                        .changedBy(producerId)
                                        .notes(note)
                                        .changedAt(OffsetDateTime.now())
                                        .build();
                                return orderStatusHistoryGateway.save(history)
                                        .then(publishStatusChanged(updated, previousStatus, newStatus, note))
                                        .thenReturn(updated);
                            });
                });
    }

    private Mono<Void> publishStatusChanged(Order order, OrderStatus previousStatus,
                                            OrderStatus newStatus, String note) {
        return userGateway.findById(order.getBuyerId())
                .map(user -> user.getEmail())
                .defaultIfEmpty("")
                .flatMap(buyerEmail -> orderEventPublisherGateway.publishStatusChanged(
                        OrderStatusChangedEvent.builder()
                                .orderId(order.getId())
                                .orderCode(order.getCode())
                                .previousStatus(previousStatus)
                                .newStatus(newStatus)
                                .buyerEmail(buyerEmail)
                                .buyerId(order.getBuyerId())
                                .totalAmount(order.getTotalAmount())
                                .note(note)
                                .changedAt(OffsetDateTime.now())
                                .build()))
                // Publishing failures must not roll back the persisted status change.
                .onErrorResume(e -> Mono.empty());
    }
}
