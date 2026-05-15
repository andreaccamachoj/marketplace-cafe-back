package co.com.marketplace.usecase.payments;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.orders.Order;
import co.com.marketplace.model.orders.OrderPayment;
import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.model.orders.PaymentStatus;
import co.com.marketplace.model.orders.gateways.OrderGateway;
import co.com.marketplace.model.orders.gateways.OrderPaymentGateway;
import co.com.marketplace.model.payments.PaymentMethod;
import co.com.marketplace.model.payments.gateways.PaymentMethodGateway;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterManualPaymentProofUseCaseTest {

    @Mock private OrderGateway orderGateway;
    @Mock private OrderPaymentGateway orderPaymentGateway;
    @Mock private PaymentMethodGateway paymentMethodGateway;

    @InjectMocks
    private RegisterManualPaymentProofUseCase useCase;

    private final UUID orderId = UUID.randomUUID();

    private Order buildOrder() {
        return Order.builder().id(orderId).buyerId(UUID.randomUUID()).code("WCM-2026-001")
                .yearlySequence(1).year(2026).subtotal(BigDecimal.TEN)
                .shippingAmount(BigDecimal.ZERO).discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.TEN).status(OrderStatus.pending_verification)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
    }

    @Test
    void execute_registersPayment_whenOrderAndMethodFound() {
        PaymentMethod method = PaymentMethod.builder().id(UUID.randomUUID()).code("transfer")
                .name("Bank Transfer").isActive(true).displayOrder(1).build();
        OrderPayment saved = OrderPayment.builder().id(UUID.randomUUID()).orderId(orderId)
                .paymentMethodId(method.getId()).paymentMethodCode("transfer")
                .amount(BigDecimal.TEN).status(PaymentStatus.submitted)
                .submittedAt(OffsetDateTime.now()).build();

        when(orderGateway.findById(orderId)).thenReturn(Mono.just(buildOrder()));
        when(paymentMethodGateway.findByCode("transfer")).thenReturn(Mono.just(method));
        when(orderPaymentGateway.save(any())).thenReturn(Mono.just(saved));

        RegisterManualPaymentProofUseCase.Command cmd = new RegisterManualPaymentProofUseCase.Command(
                orderId, "transfer", BigDecimal.TEN, "REF123", "http://proof.url/img.jpg");

        StepVerifier.create(useCase.execute(cmd))
                .expectNextMatches(p -> PaymentStatus.submitted.equals(p.getStatus()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenOrderMissing() {
        when(orderGateway.findById(orderId)).thenReturn(Mono.empty());

        RegisterManualPaymentProofUseCase.Command cmd = new RegisterManualPaymentProofUseCase.Command(
                orderId, "transfer", BigDecimal.TEN, "REF", null);

        StepVerifier.create(useCase.execute(cmd))
                .verifyError(NotFoundException.class);
    }
}
