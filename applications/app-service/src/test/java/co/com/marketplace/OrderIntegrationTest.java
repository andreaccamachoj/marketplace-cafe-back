package co.com.marketplace;

import co.com.marketplace.api.config.GlobalErrorWebExceptionHandler;
import co.com.marketplace.api.config.SecurityConfig;
import co.com.marketplace.api.orders.OrderHandler;
import co.com.marketplace.api.orders.OrderRouter;
import co.com.marketplace.model.cart.Cart;
import co.com.marketplace.model.cart.CartItem;
import co.com.marketplace.model.cart.gateways.CartGateway;
import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import co.com.marketplace.model.exception.ForbiddenException;
import co.com.marketplace.model.gateway.TokenProviderGateway;
import co.com.marketplace.model.inventory.gateways.InventoryGateway;
import co.com.marketplace.model.orders.Order;
import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.model.orders.OrderStatusHistory;
import co.com.marketplace.model.orders.gateways.OrderGateway;
import co.com.marketplace.model.orders.gateways.OrderStatusHistoryGateway;
import co.com.marketplace.model.payments.gateways.PaymentMethodGateway;
import co.com.marketplace.usecase.orders.CancelOrderUseCase;
import co.com.marketplace.usecase.orders.ConfirmOrderPaymentUseCase;
import co.com.marketplace.usecase.orders.GenerateInvoiceUseCase;
import co.com.marketplace.usecase.orders.GetOrderDetailUseCase;
import co.com.marketplace.usecase.orders.GetOrderPaymentDetailsUseCase;
import co.com.marketplace.usecase.orders.ListBuyerOrdersUseCase;
import co.com.marketplace.usecase.orders.ListOrderStatusHistoryUseCase;
import co.com.marketplace.usecase.orders.PlaceOrderUseCase;
import co.com.marketplace.usecase.payments.RegisterManualPaymentProofUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {
        OrderRouter.class, OrderHandler.class,
        SecurityConfig.class,
        GlobalErrorWebExceptionHandler.class,
        OrderIntegrationTest.RealUseCasesConfig.class
})
class OrderIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean private OrderGateway orderGateway;
    @MockitoBean private CartGateway cartGateway;
    @MockitoBean private ProductGateway productGateway;
    @MockitoBean private InventoryGateway inventoryGateway;
    @MockitoBean private PaymentMethodGateway paymentMethodGateway;
    @MockitoBean private OrderStatusHistoryGateway orderStatusHistoryGateway;
    @MockitoBean private TokenProviderGateway tokenProviderGateway;
    @MockitoBean private TransactionalOperator tx;

    @MockitoBean private ListBuyerOrdersUseCase listBuyerOrdersUseCase;
    @MockitoBean private ListOrderStatusHistoryUseCase listOrderStatusHistoryUseCase;
    @MockitoBean private GenerateInvoiceUseCase generateInvoiceUseCase;
    @MockitoBean private RegisterManualPaymentProofUseCase registerManualPaymentProofUseCase;
    @MockitoBean private GetOrderPaymentDetailsUseCase getOrderPaymentDetailsUseCase;
    @MockitoBean private ConfirmOrderPaymentUseCase confirmOrderPaymentUseCase;

    @BeforeEach
    void setupTx() {
        lenient().when(tx.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @TestConfiguration
    static class RealUseCasesConfig {
        @Bean
        PlaceOrderUseCase placeOrderUseCase(CartGateway cg, OrderGateway og,
                                             OrderStatusHistoryGateway osh, ProductGateway pg) {
            return new PlaceOrderUseCase(cg, og, osh, pg);
        }

        @Bean
        GetOrderDetailUseCase getOrderDetailUseCase(OrderGateway og) {
            return new GetOrderDetailUseCase(og);
        }

        @Bean
        CancelOrderUseCase cancelOrderUseCase(OrderGateway og, OrderStatusHistoryGateway osh) {
            return new CancelOrderUseCase(og, osh);
        }
    }

    // ── Tests ────────────────────────────────────────────────────────────────

    @Test
    void placeOrder_validCart_returns201() {
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        String token = "order-test-token";

        CartItem item = CartItem.builder()
                .id(UUID.randomUUID()).cartId(cartId).productId(productId)
                .quantity(2).unitPriceSnapshot(new BigDecimal("15.00"))
                .addedAt(OffsetDateTime.now()).build();
        Cart cart = Cart.builder().id(cartId).userId(userId).items(List.of(item)).build();
        Product product = Product.builder().id(productId).name("Café Especial")
                .price(new BigDecimal("15.00")).build();
        Order saved = Order.builder()
                .id(orderId).buyerId(userId).code("WCM-2026-001")
                .status(OrderStatus.pending_verification)
                .subtotal(new BigDecimal("30.00")).totalAmount(new BigDecimal("30.00"))
                .shippingAmount(BigDecimal.ZERO).discountAmount(BigDecimal.ZERO)
                .yearlySequence(1).year(2026)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .items(List.of()).build();

        setupAuth(token, userId);
        when(cartGateway.findByUserId(userId)).thenReturn(Mono.just(cart));
        when(orderGateway.nextYearlySequence(anyInt())).thenReturn(Mono.just(1));
        when(productGateway.findById(productId)).thenReturn(Mono.just(product));
        when(orderGateway.save(any())).thenReturn(Mono.just(saved));
        when(cartGateway.clearItems(cartId)).thenReturn(Mono.empty());
        when(orderStatusHistoryGateway.save(any())).thenReturn(Mono.just(
                OrderStatusHistory.builder().id(UUID.randomUUID()).orderId(orderId)
                        .status(OrderStatus.pending_verification)
                        .changedBy(userId).changedAt(OffsetDateTime.now()).build()));

        webTestClient.post().uri("/api/orders")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("addressId", UUID.randomUUID(), "shippingOptionId", "standard",
                        "paymentMethodCode", "TRANSFER", "notes", ""))
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.code").isEqualTo("WCM-2026-001");
    }

    @Test
    void getOrder_ownOrder_returns200() {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        String token = "order-test-token";

        Order order = Order.builder()
                .id(orderId).buyerId(userId).code("WCM-2026-002")
                .status(OrderStatus.confirmed)
                .subtotal(BigDecimal.TEN).totalAmount(BigDecimal.TEN)
                .shippingAmount(BigDecimal.ZERO).discountAmount(BigDecimal.ZERO)
                .yearlySequence(2).year(2026)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();

        setupAuth(token, userId);
        when(orderGateway.findById(orderId)).thenReturn(Mono.just(order));

        webTestClient.get().uri("/api/orders/" + orderId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(orderId.toString());
    }

    @Test
    void getOrder_foreignOrder_returns403() {
        UUID userId = UUID.randomUUID();
        UUID foreignOrderId = UUID.randomUUID();
        String token = "order-test-token";

        setupAuth(token, userId);
        when(orderGateway.findById(foreignOrderId))
                .thenReturn(Mono.error(new ForbiddenException("ORDER_FORBIDDEN",
                        "No tienes acceso a esta orden")));

        webTestClient.get().uri("/api/orders/" + foreignOrderId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void cancelOrder_cancelableOrder_returns200() {
        UUID userId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();
        String token = "order-test-token";

        Order order = Order.builder()
                .id(orderId).buyerId(userId).code("WCM-2026-003")
                .status(OrderStatus.pending_verification)
                .subtotal(BigDecimal.TEN).totalAmount(BigDecimal.TEN)
                .shippingAmount(BigDecimal.ZERO).discountAmount(BigDecimal.ZERO)
                .yearlySequence(3).year(2026)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();
        Order cancelled = order.toBuilder().status(OrderStatus.cancelled).build();

        setupAuth(token, userId);
        when(orderGateway.findById(orderId)).thenReturn(Mono.just(order));
        when(orderGateway.updateStatus(orderId, OrderStatus.cancelled)).thenReturn(Mono.just(cancelled));
        when(orderStatusHistoryGateway.save(any())).thenReturn(Mono.just(
                OrderStatusHistory.builder().id(UUID.randomUUID()).orderId(orderId)
                        .status(OrderStatus.cancelled)
                        .changedBy(userId).changedAt(OffsetDateTime.now()).build()));

        webTestClient.post().uri("/api/orders/" + orderId + "/cancel")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("reason", "Ya no lo necesito"))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("cancelled");
    }

    private void setupAuth(String token, UUID userId) {
        when(tokenProviderGateway.isTokenValid(token)).thenReturn(true);
        when(tokenProviderGateway.validateToken(token)).thenReturn(Mono.just(userId));
        when(tokenProviderGateway.extractRole(token)).thenReturn("BUYER");
    }
}
