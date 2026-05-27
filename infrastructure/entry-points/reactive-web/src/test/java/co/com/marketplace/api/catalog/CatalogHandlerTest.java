package co.com.marketplace.api.catalog;

import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.ProductStatus;
import co.com.marketplace.usecase.catalog.GetCategoryBySlugUseCase;
import co.com.marketplace.usecase.catalog.GetFeaturedProductsUseCase;
import co.com.marketplace.usecase.catalog.GetProductByIdUseCase;
import co.com.marketplace.usecase.catalog.GetProductBySlugUseCase;
import co.com.marketplace.usecase.catalog.ListCategoriesUseCase;
import co.com.marketplace.usecase.catalog.ListCertificationsUseCase;
import co.com.marketplace.usecase.catalog.ListProductsUseCase;
import co.com.marketplace.usecase.catalog.ListRoastLevelsUseCase;
import co.com.marketplace.usecase.catalog.SearchProductsUseCase;
import co.com.marketplace.usecase.reviews.ListProductReviewsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {CatalogRouter.class, CatalogHandler.class})
class CatalogHandlerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean private ListProductsUseCase listProductsUseCase;
    @MockitoBean private GetFeaturedProductsUseCase getFeaturedProductsUseCase;
    @MockitoBean private GetProductByIdUseCase getProductByIdUseCase;
    @MockitoBean private GetProductBySlugUseCase getProductBySlugUseCase;
    @MockitoBean private ListCategoriesUseCase listCategoriesUseCase;
    @MockitoBean private GetCategoryBySlugUseCase getCategoryBySlugUseCase;
    @MockitoBean private ListCertificationsUseCase listCertificationsUseCase;
    @MockitoBean private ListRoastLevelsUseCase listRoastLevelsUseCase;
    @MockitoBean private SearchProductsUseCase searchProductsUseCase;
    @MockitoBean private ListProductReviewsUseCase listProductReviewsUseCase;

    private Product buildProduct() {
        return Product.builder()
                .id(UUID.randomUUID()).name("Test Coffee").price(BigDecimal.valueOf(20.00))
                .status(ProductStatus.active).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void listProducts_returns200() {
        when(listProductsUseCase.execute(any())).thenReturn(Flux.just(buildProduct()));

        webTestClient.get()
                .uri("/api/catalog/products")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("Test Coffee");
    }

    @Test
    void getFeatured_returns200() {
        when(getFeaturedProductsUseCase.execute(anyInt())).thenReturn(Flux.just(buildProduct()));

        webTestClient.get()
                .uri("/api/catalog/products/featured")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("Test Coffee");
    }
}
