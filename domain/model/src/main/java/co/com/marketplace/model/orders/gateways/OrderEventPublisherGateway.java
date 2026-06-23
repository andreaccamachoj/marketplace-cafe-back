package co.com.marketplace.model.orders.gateways;

import co.com.marketplace.model.orders.OrderStatusChangedEvent;
import reactor.core.publisher.Mono;

public interface OrderEventPublisherGateway {
    Mono<Void> publishStatusChanged(OrderStatusChangedEvent event);
}
