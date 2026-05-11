package co.com.marketplace.usecase.orders;

import co.com.marketplace.model.orders.Order;
import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.model.orders.gateways.OrderGateway;
import reactor.core.publisher.Flux;

import java.util.UUID;

public final class ListProducerOrdersUseCase {

    private final OrderGateway orderGateway;

    public ListProducerOrdersUseCase(OrderGateway orderGateway) {
        this.orderGateway = orderGateway;
    }

    public Flux<Order> execute(UUID producerId, OrderStatus status, int page, int size) {
        return orderGateway.findByProducerId(producerId, status, page, size);
    }
}
