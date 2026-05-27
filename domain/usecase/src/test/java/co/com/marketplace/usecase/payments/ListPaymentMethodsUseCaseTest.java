package co.com.marketplace.usecase.payments;

import co.com.marketplace.model.payments.PaymentMethod;
import co.com.marketplace.model.payments.gateways.PaymentMethodGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListPaymentMethodsUseCaseTest {

    @Mock private PaymentMethodGateway paymentMethodGateway;

    @InjectMocks
    private ListPaymentMethodsUseCase useCase;

    @Test
    void execute_returnsActivePaymentMethods() {
        PaymentMethod pm = PaymentMethod.builder().id(UUID.randomUUID()).code("transfer")
                .name("Bank Transfer").isActive(true).displayOrder(1).build();
        when(paymentMethodGateway.findAllActive()).thenReturn(Flux.just(pm));

        StepVerifier.create(useCase.execute())
                .expectNextMatches(m -> "transfer".equals(m.getCode()))
                .verifyComplete();
    }

    @Test
    void execute_returnsEmpty_whenNone() {
        when(paymentMethodGateway.findAllActive()).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute())
                .verifyComplete();
    }
}
