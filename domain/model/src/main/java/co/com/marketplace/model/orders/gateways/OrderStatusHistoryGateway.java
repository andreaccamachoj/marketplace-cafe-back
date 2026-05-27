package co.com.marketplace.model.orders.gateways;

import co.com.marketplace.model.orders.OrderStatusHistory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderStatusHistoryGateway {
    Mono<OrderStatusHistory> save(OrderStatusHistory entry);
    Flux<OrderStatusHistory> findByOrderId(UUID orderId);
}
