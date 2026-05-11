package co.com.marketplace.usecase.orders;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.orders.Order;
import co.com.marketplace.model.orders.gateways.OrderGateway;
import reactor.core.publisher.Mono;

import java.util.UUID;

public final class GetOrderDetailUseCase {

    private final OrderGateway orderGateway;

    public GetOrderDetailUseCase(OrderGateway orderGateway) {
        this.orderGateway = orderGateway;
    }

    public Mono<Order> execute(UUID orderId) {
        return orderGateway.findById(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("ORDER_NOT_FOUND", "Order not found: " + orderId)));
    }
}
