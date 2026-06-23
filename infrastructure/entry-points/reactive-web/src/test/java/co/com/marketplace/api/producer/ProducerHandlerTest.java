package co.com.marketplace.api.producer;

import co.com.marketplace.api.config.GlobalErrorWebExceptionHandler;
import co.com.marketplace.api.config.TestTxConfig;
import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.ProductStatus;
import co.com.marketplace.model.farm.Farm;
import co.com.marketplace.model.orders.Order;
import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.usecase.catalog.ArchiveProductUseCase;
import co.com.marketplace.usecase.catalog.CreateProductUseCase;
import co.com.marketplace.usecase.catalog.DeleteProductCoverImageUseCase;
import co.com.marketplace.usecase.catalog.GetProductsByProducerUseCase;
import co.com.marketplace.usecase.catalog.ListMyProductsUseCase;
import co.com.marketplace.usecase.catalog.UpdateProductCoverImageUseCase;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {ProducerRouter.class, ProducerHandler.class, GlobalErrorWebExceptionHandler.class, TestTxConfig.class})
class ProducerHandlerTest {

    private static final String USER_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Autowired private WebTestClient webTestClient;

    @MockitoBean private ListMyProductsUseCase listMyProductsUseCase;
    @MockitoBean private CreateProductUseCase createProductUseCase;
    @MockitoBean private UpdateProductUseCase updateProductUseCase;
    @MockitoBean private ArchiveProductUseCase archiveProductUseCase;
    @MockitoBean private UpdateProductCoverImageUseCase updateProductCoverImageUseCase;
    @MockitoBean private DeleteProductCoverImageUseCase deleteProductCoverImageUseCase;
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
                .id(UUID.randomUUID()).name("Test Coffee")
                .price(BigDecimal.valueOf(20)).status(ProductStatus.active)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();
    }

    private Order buildOrder() {
        return Order.builder()
                .id(UUID.randomUUID()).buyerId(UUID.fromString(USER_ID))
                .status(OrderStatus.pending_verification).totalAmount(BigDecimal.TEN)
                .items(List.of()).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();
    }

    private Farm buildFarm() {
        return Farm.builder().id(UUID.randomUUID()).producerId(UUID.fromString(USER_ID))
                .name("Test Farm").createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void listMyProducts_returns200() {
        when(listMyProductsUseCase.execute(any(), any(), anyInt(), anyInt()))
                .thenReturn(Flux.just(buildProduct()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .get().uri("/api/producer/products")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void updateProduct_returns200() {
        when(updateProductUseCase.execute(any(), any(), any())).thenReturn(Mono.just(buildProduct()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .put().uri("/api/producer/products/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name":"Updated","description":"desc","price":25.00,"unit":"kg","region":"Huila","emoji":"",
                        "categoryId":"550e8400-e29b-41d4-a716-446655440001","stock":5,"status":"active","certifications":[]}
                        """)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void archiveProduct_returns204() {
        when(archiveProductUseCase.execute(any(), any())).thenReturn(Mono.empty());

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .post().uri("/api/producer/products/" + UUID.randomUUID() + "/archive")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void listOrders_returns200() {
        when(listProducerOrdersUseCase.execute(any(), any(), anyInt(), anyInt()))
                .thenReturn(Flux.just(buildOrder()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .get().uri("/api/producer/orders")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getFarm_returns200() {
        when(getFarmProfileUseCase.execute(any())).thenReturn(Mono.just(buildFarm()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .get().uri("/api/producer/farm")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void listReviews_returns200() {
        when(listProducerReviewsUseCase.execute(any(), anyInt(), anyInt())).thenReturn(Flux.empty());

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .get().uri("/api/producer/reviews")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getFarmCertifications_returns200() {
        when(getFarmCertificationsUseCase.execute(any())).thenReturn(Flux.empty());

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .get().uri("/api/producer/farm/certifications")
                .exchange()
                .expectStatus().isOk();
    }
}
