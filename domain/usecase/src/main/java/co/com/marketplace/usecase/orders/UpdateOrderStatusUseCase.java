package co.com.marketplace.usecase.orders;

import co.com.marketplace.model.exception.NotFoundException;
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
public final class UpdateOrderStatusUseCase {

    private final OrderGateway orderGateway;
    private final OrderStatusHistoryGateway orderStatusHistoryGateway;

    public Mono<Order> execute(UUID orderId, UUID producerId, OrderStatus newStatus, String note) {
        return orderGateway.findById(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("ORDER_NOT_FOUND", "Order not found: " + orderId)))
                .flatMap(order -> orderGateway.updateStatus(orderId, newStatus)
                        .flatMap(updated -> {
                            OrderStatusHistory history = OrderStatusHistory.builder()
                                    .id(UUID.randomUUID())
                                    .orderId(orderId)
                                    .status(newStatus)
                                    .changedBy(producerId)
                                    .notes(note)
                                    .changedAt(OffsetDateTime.now())
                                    .build();
                            return orderStatusHistoryGateway.save(history).thenReturn(updated);
                        }));
    }
}
