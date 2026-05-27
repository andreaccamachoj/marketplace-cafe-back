package co.com.marketplace.usecase.payments;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.orders.OrderPayment;
import co.com.marketplace.model.orders.PaymentStatus;
import co.com.marketplace.model.orders.gateways.OrderGateway;
import co.com.marketplace.model.orders.gateways.OrderPaymentGateway;
import co.com.marketplace.model.payments.gateways.PaymentMethodGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public final class RegisterManualPaymentProofUseCase {

    private final OrderGateway orderGateway;
    private final OrderPaymentGateway orderPaymentGateway;
    private final PaymentMethodGateway paymentMethodGateway;

    public record Command(
            UUID orderId,
            String paymentMethodCode,
            BigDecimal amount,
            String reference,
            String proofUrl
    ) {}

    public Mono<OrderPayment> execute(Command cmd) {
        return orderGateway.findById(cmd.orderId())
                .switchIfEmpty(Mono.error(new NotFoundException("ORDER_NOT_FOUND", "Order not found: " + cmd.orderId())))
                .flatMap(order -> paymentMethodGateway.findByCode(cmd.paymentMethodCode())
                        .switchIfEmpty(Mono.error(new NotFoundException("PAYMENT_METHOD_NOT_FOUND", "Payment method not found: " + cmd.paymentMethodCode())))
                        .flatMap(method -> {
                            OrderPayment payment = OrderPayment.builder()
                                    .id(UUID.randomUUID())
                                    .orderId(cmd.orderId())
                                    .paymentMethodId(method.getId())
                                    .paymentMethodCode(cmd.paymentMethodCode())
                                    .amount(cmd.amount())
                                    .status(PaymentStatus.submitted)
                                    .reference(cmd.reference())
                                    .proofUrl(cmd.proofUrl())
                                    .submittedAt(OffsetDateTime.now())
                                    .build();
                            return orderPaymentGateway.save(payment);
                        }));
    }
}
