package co.com.marketplace.usecase.orders;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.orders.OrderPayment;
import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.model.orders.PaymentStatus;
import co.com.marketplace.model.orders.gateways.OrderGateway;
import co.com.marketplace.model.orders.gateways.OrderPaymentGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public final class ConfirmOrderPaymentUseCase {

    private final OrderPaymentGateway orderPaymentGateway;
    private final OrderGateway orderGateway;

    public Mono<OrderPayment> execute(UUID orderId, UUID adminId, boolean verified, String note) {
        return orderPaymentGateway.findByOrderId(orderId)
                .switchIfEmpty(Mono.error(new NotFoundException("PAYMENT_NOT_FOUND", "Payment not found for order: " + orderId)))
                .flatMap(payment -> {
                    PaymentStatus newPaymentStatus = verified ? PaymentStatus.verified : PaymentStatus.rejected;
                    OrderStatus newOrderStatus = verified ? OrderStatus.confirmed : OrderStatus.cancelled;
                    return orderPaymentGateway.updateStatus(payment.getId(), newPaymentStatus, adminId)
                            .flatMap(updated -> orderGateway.updateStatus(orderId, newOrderStatus)
                                    .thenReturn(updated));
                });
    }
}
