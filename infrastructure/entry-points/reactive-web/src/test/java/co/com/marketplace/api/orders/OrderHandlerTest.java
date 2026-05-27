package co.com.marketplace.api.orders;

import co.com.marketplace.api.config.GlobalErrorWebExceptionHandler;
import co.com.marketplace.api.config.TestTxConfig;
import co.com.marketplace.model.orders.Order;
import co.com.marketplace.model.orders.OrderPayment;
import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.model.orders.PaymentStatus;
import co.com.marketplace.usecase.orders.CancelOrderUseCase;
import co.com.marketplace.usecase.orders.ConfirmOrderPaymentUseCase;
import co.com.marketplace.usecase.orders.GenerateInvoiceUseCase;
import co.com.marketplace.usecase.orders.GetOrderDetailUseCase;
import co.com.marketplace.usecase.orders.GetOrderPaymentDetailsUseCase;
import co.com.marketplace.usecase.orders.ListBuyerOrdersUseCase;
import co.com.marketplace.usecase.orders.ListOrderStatusHistoryUseCase;
import co.com.marketplace.usecase.orders.PlaceOrderUseCase;
import co.com.marketplace.usecase.payments.RegisterManualPaymentProofUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {OrderRouter.class, OrderHandler.class, GlobalErrorWebExceptionHandler.class, TestTxConfig.class})
class OrderHandlerTest {

    private static final String USER_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Autowired private WebTestClient webTestClient;

    @MockitoBean private PlaceOrderUseCase placeOrderUseCase;
    @MockitoBean private ListBuyerOrdersUseCase listBuyerOrdersUseCase;
    @MockitoBean private GetOrderDetailUseCase getOrderDetailUseCase;
    @MockitoBean private CancelOrderUseCase cancelOrderUseCase;
    @MockitoBean private ListOrderStatusHistoryUseCase listOrderStatusHistoryUseCase;
    @MockitoBean private GenerateInvoiceUseCase generateInvoiceUseCase;
    @MockitoBean private RegisterManualPaymentProofUseCase registerManualPaymentProofUseCase;
    @MockitoBean private GetOrderPaymentDetailsUseCase getOrderPaymentDetailsUseCase;
    @MockitoBean private ConfirmOrderPaymentUseCase confirmOrderPaymentUseCase;

    private Order buildOrder() {
        return Order.builder()
                .id(UUID.randomUUID()).buyerId(UUID.fromString(USER_ID))
                .status(OrderStatus.pending_verification).totalAmount(BigDecimal.TEN)
                .items(List.of()).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();
    }

    private OrderPayment buildPayment() {
        return OrderPayment.builder()
                .id(UUID.randomUUID()).orderId(UUID.randomUUID())
                .status(PaymentStatus.submitted).amount(BigDecimal.TEN)
                .build();
    }

    @Test
    void listOrders_returns200() {
        when(listBuyerOrdersUseCase.execute(any(), any(), anyInt(), anyInt()))
                .thenReturn(Flux.just(buildOrder()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .get().uri("/api/orders")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getOrder_returns200() {
        when(getOrderDetailUseCase.execute(any())).thenReturn(Mono.just(buildOrder()));

        webTestClient.get().uri("/api/orders/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void cancelOrder_returns200() {
        when(cancelOrderUseCase.execute(any(), any(), anyString())).thenReturn(Mono.just(buildOrder()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .post().uri("/api/orders/" + UUID.randomUUID() + "/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"reason":"no longer needed"}
                        """)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getTimeline_returns200() {
        when(listOrderStatusHistoryUseCase.execute(any())).thenReturn(Flux.empty());

        webTestClient.get().uri("/api/orders/" + UUID.randomUUID() + "/timeline")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getInvoice_returns200() {
        when(generateInvoiceUseCase.execute(any())).thenReturn(Mono.just(buildOrder()));

        webTestClient.get().uri("/api/orders/" + UUID.randomUUID() + "/invoice")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getPayment_returns200() {
        when(getOrderPaymentDetailsUseCase.execute(any())).thenReturn(Mono.just(buildPayment()));

        webTestClient.get().uri("/api/orders/" + UUID.randomUUID() + "/payment")
                .exchange()
                .expectStatus().isOk();
    }
}
