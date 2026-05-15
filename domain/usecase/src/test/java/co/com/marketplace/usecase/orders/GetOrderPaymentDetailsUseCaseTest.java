package co.com.marketplace.usecase.orders;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.orders.OrderPayment;
import co.com.marketplace.model.orders.PaymentStatus;
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
class GetOrderPaymentDetailsUseCaseTest {

    @Mock private OrderPaymentGateway orderPaymentGateway;

    @InjectMocks
    private GetOrderPaymentDetailsUseCase useCase;

    private final UUID orderId = UUID.randomUUID();

    @Test
    void execute_returnsPayment_whenFound() {
        OrderPayment payment = OrderPayment.builder().id(UUID.randomUUID()).orderId(orderId)
                .status(PaymentStatus.submitted).amount(BigDecimal.TEN)
                .submittedAt(OffsetDateTime.now()).build();
        when(orderPaymentGateway.findByOrderId(orderId)).thenReturn(Mono.just(payment));

        StepVerifier.create(useCase.execute(orderId))
                .expectNextMatches(p -> orderId.equals(p.getOrderId()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenMissing() {
        when(orderPaymentGateway.findByOrderId(orderId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(orderId))
                .verifyError(NotFoundException.class);
    }
}
