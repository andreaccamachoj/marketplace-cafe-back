package co.com.marketplace.usecase.orders;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.orders.OrderPayment;
import co.com.marketplace.model.orders.gateways.OrderPaymentGateway;
import reactor.core.publisher.Mono;

import java.util.UUID;

public final class GetOrderPaymentDetailsUseCase {

    private final OrderPaymentGateway orderPaymentGateway;

    public GetOrderPaymentDetailsUseCase(OrderPaymentGateway orderPaymentGateway) {
        this.orderPaymentGateway = orderPaymentGateway;
    }

    public Mono<OrderPayment> execute(UUID orderId) {
        return orderPaymentGateway.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("PAYMENT_NOT_FOUND", "Payment not found for order: " + orderId)));
    }
}
