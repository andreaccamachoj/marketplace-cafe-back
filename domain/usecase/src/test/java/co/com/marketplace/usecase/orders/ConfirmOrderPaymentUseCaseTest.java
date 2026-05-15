package co.com.marketplace.usecase.orders;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.orders.Order;
import co.com.marketplace.model.orders.OrderPayment;
import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.model.orders.PaymentStatus;
import co.com.marketplace.model.orders.gateways.OrderGateway;
import co.com.marketplace.model.orders.gateways.OrderPaymentGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmOrderPaymentUseCaseTest {

    @Mock private OrderPaymentGateway orderPaymentGateway;
    @Mock private OrderGateway orderGateway;

    @InjectMocks
    private ConfirmOrderPaymentUseCase useCase;

    private final UUID orderId = UUID.randomUUID();
    private final UUID adminId = UUID.randomUUID();
    private final UUID paymentId = UUID.randomUUID();

    @Test
    void execute_verifiesPayment_andConfirmsOrder() {
        OrderPayment payment = OrderPayment.builder().id(paymentId).orderId(orderId)
                .status(PaymentStatus.submitted).amount(BigDecimal.TEN)
                .submittedAt(OffsetDateTime.now()).build();
        OrderPayment verified = payment.toBuilder().status(PaymentStatus.verified).verifiedBy(adminId).build();
        Order order = Order.builder().id(orderId).buyerId(UUID.randomUUID()).code("WCM-2026-001")
                .yearlySequence(1).year(2026).subtotal(BigDecimal.TEN)
                .shippingAmount(BigDecimal.ZERO).discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.TEN).status(OrderStatus.confirmed)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();

        when(orderPaymentGateway.findByOrderId(orderId)).thenReturn(Mono.just(payment));
        when(orderPaymentGateway.updateStatus(paymentId, PaymentStatus.verified, adminId)).thenReturn(Mono.just(verified));
        when(orderGateway.updateStatus(orderId, OrderStatus.confirmed)).thenReturn(Mono.just(order));

        StepVerifier.create(useCase.execute(orderId, adminId, true, "Verified"))
                .expectNextMatches(p -> PaymentStatus.verified.equals(p.getStatus()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenPaymentMissing() {
        when(orderPaymentGateway.findByOrderId(orderId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(orderId, adminId, true, null))
                .verifyError(NotFoundException.class);
    }
}
