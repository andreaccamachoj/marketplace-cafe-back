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
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {OrderRouter.class, OrderHandler.class, GlobalErrorWebExceptionHandler.class, TestTxConfig.class})
class OrderHandlerPlaceOrderTest {

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
                .status(OrderStatus.pending_verification).totalAmount(BigDecimal.valueOf(58000))
                .items(List.of()).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();
    }

    private OrderPayment buildPayment() {
        return OrderPayment.builder()
                .id(UUID.randomUUID()).orderId(UUID.randomUUID())
                .status(PaymentStatus.submitted).amount(BigDecimal.valueOf(58000))
                .paymentMethodCode("NEQUI")
                .build();
    }

    @Test
    void placeOrder_returns201() {
        when(placeOrderUseCase.execute(any())).thenReturn(Mono.just(buildOrder()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .post().uri("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"addressId":"550e8400-e29b-41d4-a716-446655440001",
                         "shippingOptionId":"standard",
                         "paymentMethodCode":"NEQUI",
                         "notes":"Llamar antes de entregar"}
                        """)
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void submitPaymentProof_returns200() {
        when(registerManualPaymentProofUseCase.execute(any())).thenReturn(Mono.just(buildPayment()));

        webTestClient.post()
                .uri("/api/orders/" + UUID.randomUUID() + "/payment-proof")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"paymentMethodCode":"NEQUI","amount":58000,
                         "reference":"TRX-12345","proofUrl":"https://storage/proof.jpg"}
                        """)
                .exchange()
                .expectStatus().isOk();
    }
}
