package co.com.marketplace.usecase.orders;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.exception.ValidationException;
import co.com.marketplace.model.orders.Order;
import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.model.orders.OrderStatusHistory;
import co.com.marketplace.model.orders.gateways.OrderGateway;
import co.com.marketplace.model.orders.gateways.OrderStatusHistoryGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public final class CancelOrderUseCase {

    private final OrderGateway orderGateway;
    private final OrderStatusHistoryGateway orderStatusHistoryGateway;

    public Mono<Order> execute(UUID orderId, UUID buyerId, String reason) {
        return orderGateway.findById(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("ORDER_NOT_FOUND", "Order not found: " + orderId)))
                .flatMap(order -> {
                    if (order.getStatus() != OrderStatus.pending_verification
                            && order.getStatus() != OrderStatus.confirmed) {
                        return Mono.error(new ValidationException(
                                "ORDER_CANNOT_BE_CANCELLED", "Order cannot be cancelled in status: " + order.getStatus()));
                    }
                    return orderGateway.updateStatus(orderId, OrderStatus.cancelled)
                            .flatMap(updated -> {
                                OrderStatusHistory history = OrderStatusHistory.builder()
                                        .id(UUID.randomUUID())
                                        .orderId(orderId)
                                        .status(OrderStatus.cancelled)
                                        .changedBy(buyerId)
                                        .notes(reason)
                                        .changedAt(OffsetDateTime.now())
                                        .build();
                                return orderStatusHistoryGateway.save(history).thenReturn(updated);
                            });
                });
    }
}
