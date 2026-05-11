package co.com.marketplace.usecase.orders;

import co.com.marketplace.model.orders.Order;
import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.model.orders.gateways.OrderGateway;
import reactor.core.publisher.Flux;

import java.util.UUID;

public final class ListBuyerOrdersUseCase {

    private final OrderGateway orderGateway;

    public ListBuyerOrdersUseCase(OrderGateway orderGateway) {
        this.orderGateway = orderGateway;
    }

    public Flux<Order> execute(UUID buyerId, OrderStatus status, int page, int size) {
        return orderGateway.findByBuyerId(buyerId, status, page, size);
    }
}
