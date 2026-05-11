package co.com.marketplace.usecase.orders;

import co.com.marketplace.model.orders.OrderStatusHistory;
import co.com.marketplace.model.orders.gateways.OrderStatusHistoryGateway;
import reactor.core.publisher.Flux;

import java.util.UUID;

public final class ListOrderStatusHistoryUseCase {

    private final OrderStatusHistoryGateway orderStatusHistoryGateway;

    public ListOrderStatusHistoryUseCase(OrderStatusHistoryGateway orderStatusHistoryGateway) {
        this.orderStatusHistoryGateway = orderStatusHistoryGateway;
    }

    public Flux<OrderStatusHistory> execute(UUID orderId) {
        return orderStatusHistoryGateway.findByOrderId(orderId);
    }
}
