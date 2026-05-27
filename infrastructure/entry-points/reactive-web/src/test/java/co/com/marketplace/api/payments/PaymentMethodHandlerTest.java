package co.com.marketplace.api.payments;

import co.com.marketplace.api.config.GlobalErrorWebExceptionHandler;
import co.com.marketplace.model.payments.PaymentMethod;
import co.com.marketplace.usecase.payments.ListPaymentMethodsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.UUID;

import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {PaymentMethodRouter.class, PaymentMethodHandler.class, GlobalErrorWebExceptionHandler.class})
class PaymentMethodHandlerTest {

    @Autowired private WebTestClient webTestClient;

    @MockitoBean private ListPaymentMethodsUseCase listPaymentMethodsUseCase;

    @Test
    void list_returns200() {
        when(listPaymentMethodsUseCase.execute()).thenReturn(Flux.just(
                PaymentMethod.builder().id(UUID.randomUUID()).code("CASH").name("Cash").type("manual").build()));

        webTestClient.get().uri("/api/payment-methods")
                .exchange()
                .expectStatus().isOk();
    }
}
