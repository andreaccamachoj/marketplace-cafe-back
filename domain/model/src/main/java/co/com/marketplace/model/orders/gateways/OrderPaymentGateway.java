package co.com.marketplace.model.orders.gateways;

import co.com.marketplace.model.orders.OrderPayment;
import co.com.marketplace.model.orders.PaymentStatus;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderPaymentGateway {
    Mono<OrderPayment> save(OrderPayment payment);
    Mono<OrderPayment> findByOrderId(UUID orderId);
    Mono<OrderPayment> updateStatus(UUID id, PaymentStatus status, UUID verifiedBy);
}
