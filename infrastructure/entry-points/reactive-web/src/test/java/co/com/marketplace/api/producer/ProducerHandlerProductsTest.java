package co.com.marketplace.api.producer;

import co.com.marketplace.api.config.GlobalErrorWebExceptionHandler;
import co.com.marketplace.api.config.TestTxConfig;
import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.ProductStatus;
import co.com.marketplace.model.farm.Farm;
import co.com.marketplace.model.farm.FarmCertification;
import co.com.marketplace.model.inventory.InventoryItem;
import co.com.marketplace.model.orders.Order;
import co.com.marketplace.model.orders.OrderPayment;
import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.model.orders.PaymentStatus;
import co.com.marketplace.usecase.catalog.ArchiveProductUseCase;
import co.com.marketplace.usecase.catalog.CreateProductUseCase;
import co.com.marketplace.usecase.catalog.GetProductsByProducerUseCase;
import co.com.marketplace.usecase.catalog.ListMyProductsUseCase;
import co.com.marketplace.usecase.catalog.UpdateProductUseCase;
import co.com.marketplace.usecase.farm.AddFarmCertificationUseCase;
import co.com.marketplace.usecase.farm.GetFarmCertificationsUseCase;
import co.com.marketplace.usecase.farm.GetFarmProfileUseCase;
import co.com.marketplace.usecase.farm.RemoveFarmCertificationUseCase;
import co.com.marketplace.usecase.farm.UpdateFarmProfileUseCase;
import co.com.marketplace.usecase.inventory.AdjustInventoryUseCase;
import co.com.marketplace.usecase.inventory.GetInventoryByProductUseCase;
import co.com.marketplace.usecase.orders.ConfirmOrderPaymentUseCase;
import co.com.marketplace.usecase.orders.ListProducerOrdersUseCase;
import co.com.marketplace.usecase.orders.UpdateOrderStatusUseCase;
import co.com.marketplace.usecase.reviews.ListProducerReviewsUseCase;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {ProducerRouter.class, ProducerHandler.class, GlobalErrorWebExceptionHandler.class, TestTxConfig.class})
class ProducerHandlerProductsTest {

    private static final String USER_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Autowired private WebTestClient webTestClient;

    @MockitoBean private ListMyProductsUseCase listMyProductsUseCase;
    @MockitoBean private CreateProductUseCase createProductUseCase;
    @MockitoBean private UpdateProductUseCase updateProductUseCase;
    @MockitoBean private ArchiveProductUseCase archiveProductUseCase;
    @MockitoBean private GetProductsByProducerUseCase getProductsByProducerUseCase;
    @MockitoBean private ListProducerOrdersUseCase listProducerOrdersUseCase;
    @MockitoBean private UpdateOrderStatusUseCase updateOrderStatusUseCase;
    @MockitoBean private ConfirmOrderPaymentUseCase confirmOrderPaymentUseCase;
    @MockitoBean private GetFarmProfileUseCase getFarmProfileUseCase;
    @MockitoBean private UpdateFarmProfileUseCase updateFarmProfileUseCase;
    @MockitoBean private ListProducerReviewsUseCase listProducerReviewsUseCase;
    @MockitoBean private GetInventoryByProductUseCase getInventoryByProductUseCase;
    @MockitoBean private AdjustInventoryUseCase adjustInventoryUseCase;
    @MockitoBean private AddFarmCertificationUseCase addFarmCertificationUseCase;
    @MockitoBean private GetFarmCertificationsUseCase getFarmCertificationsUseCase;
    @MockitoBean private RemoveFarmCertificationUseCase removeFarmCertificationUseCase;

    private Product buildProduct() {
        return Product.builder()
                .id(UUID.randomUUID()).name("Café Supremo").price(BigDecimal.valueOf(30000))
                .status(ProductStatus.active).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();
    }

    private Order buildOrder() {
        return Order.builder()
                .id(UUID.randomUUID()).buyerId(UUID.randomUUID())
                .status(OrderStatus.confirmed).totalAmount(BigDecimal.valueOf(80000))
                .items(List.of()).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();
    }

    private Farm buildFarm() {
        return Farm.builder().id(UUID.randomUUID()).producerId(UUID.fromString(USER_ID))
                .name("Finca Cafetera").createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void createProduct_returns201() {
        when(createProductUseCase.execute(any())).thenReturn(Mono.just(buildProduct()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .post().uri("/api/producer/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"categoryId":"550e8400-e29b-41d4-a716-446655440001","name":"Café Supremo",
                         "description":"desc","price":30000,"unit":"kg","region":"Huila",
                         "emoji":"☕","stock":50,"status":"active","certifications":[]}
                        """)
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void updateOrderStatus_returns200() {
        when(updateOrderStatusUseCase.execute(any(), any(), any(), any())).thenReturn(Mono.just(buildOrder()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .patch().uri("/api/producer/orders/" + UUID.randomUUID() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"newStatus":"confirmed","note":"Pedido confirmado"}
                        """)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void confirmPayment_returns200() {
        OrderPayment payment = OrderPayment.builder()
                .id(UUID.randomUUID()).orderId(UUID.randomUUID())
                .status(PaymentStatus.verified).amount(BigDecimal.valueOf(80000))
                .build();
        when(confirmOrderPaymentUseCase.execute(any(), any(), anyBoolean(), any())).thenReturn(Mono.just(payment));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .post().uri("/api/producer/orders/" + UUID.randomUUID() + "/payment/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"verified":true,"note":"Pago recibido"}
                        """)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void updateFarm_returns200() {
        when(updateFarmProfileUseCase.execute(any(), any())).thenReturn(Mono.just(buildFarm()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .patch().uri("/api/producer/farm")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name":"Finca Cafetera","municipality":"Salento","department":"Quindio",
                         "altitudeMasl":1800,"areaHectares":5.5,"mainVariety":"Castillo",
                         "process":"washed","description":"desc"}
                        """)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getInventory_byProductId_returns200() {
        InventoryItem item = InventoryItem.builder()
                .id(UUID.randomUUID()).productId(UUID.randomUUID())
                .quantity(100).maxStock(500).updatedAt(OffsetDateTime.now())
                .build();
        when(getInventoryByProductUseCase.execute(any())).thenReturn(Mono.just(item));

        UUID productId = UUID.randomUUID();
        webTestClient.get()
                .uri("/api/producer/inventory?productId=" + productId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getInventory_allProducts_returns200() {
        when(getProductsByProducerUseCase.execute(any(), anyInt(), anyInt()))
                .thenReturn(Flux.just(buildProduct()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .get().uri("/api/producer/inventory")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void adjustInventory_returns200() {
        InventoryItem item = InventoryItem.builder()
                .id(UUID.randomUUID()).productId(UUID.randomUUID())
                .quantity(90).maxStock(500).updatedAt(OffsetDateTime.now())
                .build();
        when(adjustInventoryUseCase.execute(any(), anyInt())).thenReturn(Mono.just(item));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .post().uri("/api/producer/inventory/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"productId":"550e8400-e29b-41d4-a716-446655440002","delta":-10}
                        """)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void addFarmCertification_returns201() {
        FarmCertification cert = FarmCertification.builder()
                .id(UUID.randomUUID()).farmId(UUID.randomUUID())
                .certificationId(1).issuer("USDA").build();
        when(addFarmCertificationUseCase.execute(any(), any())).thenReturn(Mono.just(cert));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .post().uri("/api/producer/farm/certifications")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"type":"ORGANIC","name":"Orgánico","issuer":"USDA","expiryDate":"2025-12-31"}
                        """)
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void removeFarmCertification_returns204() {
        when(removeFarmCertificationUseCase.execute(any())).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/producer/farm/certifications/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isNoContent();
    }
}
