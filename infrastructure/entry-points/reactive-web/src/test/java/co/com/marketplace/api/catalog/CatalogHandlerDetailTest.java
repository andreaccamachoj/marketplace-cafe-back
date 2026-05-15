package co.com.marketplace.api.catalog;

import co.com.marketplace.model.catalog.Category;
import co.com.marketplace.model.catalog.Certification;
import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.ProductStatus;
import co.com.marketplace.model.catalog.RoastLevel;
import co.com.marketplace.model.reviews.Review;
import co.com.marketplace.model.reviews.ReviewStatus;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {CatalogRouter.class, CatalogHandler.class})
class CatalogHandlerDetailTest {

    @Autowired private WebTestClient webTestClient;

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

    private final UUID productId = UUID.randomUUID();

    private Product buildProduct() {
        return Product.builder()
                .id(productId).name("Café Especial").price(BigDecimal.valueOf(35000))
                .status(ProductStatus.active).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();
    }

    private Category buildCategory() {
        return Category.builder()
                .id(UUID.randomUUID()).name("Café en Grano").slug("cafe-en-grano")
                .isActive(true).createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void getProductById_returns200() {
        when(getProductByIdUseCase.execute(any())).thenReturn(Mono.just(buildProduct()));

        webTestClient.get()
                .uri("/api/catalog/products/" + productId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Café Especial");
    }

    @Test
    void getProductBySlug_withUUID_returns200() {
        when(getProductBySlugUseCase.execute(any())).thenReturn(Mono.just(buildProduct()));

        webTestClient.get()
                .uri("/api/catalog/products/by-slug/" + productId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void getProductBySlug_withNonUUID_returns404() {
        webTestClient.get()
                .uri("/api/catalog/products/by-slug/not-a-uuid")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void getProductReviews_returns200() {
        Review review = Review.builder()
                .id(UUID.randomUUID()).productId(productId).buyerId(UUID.randomUUID())
                .rating((short) 4).status(ReviewStatus.published)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();
        when(listProductReviewsUseCase.execute(any(), anyInt(), anyInt())).thenReturn(Flux.just(review));

        webTestClient.get()
                .uri("/api/catalog/products/" + productId + "/reviews")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void listCategories_returns200() {
        when(listCategoriesUseCase.execute()).thenReturn(Flux.just(buildCategory()));

        webTestClient.get()
                .uri("/api/catalog/categories")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].slug").isEqualTo("cafe-en-grano");
    }

    @Test
    void getCategoryBySlug_returns200() {
        when(getCategoryBySlugUseCase.execute(anyString())).thenReturn(Mono.just(buildCategory()));

        webTestClient.get()
                .uri("/api/catalog/categories/cafe-en-grano")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void listCertifications_returns200() {
        Certification cert = Certification.builder()
                .id(1).code("ORGANIC").name("Orgánico").issuingBody("USDA").build();
        when(listCertificationsUseCase.execute()).thenReturn(Flux.just(cert));

        webTestClient.get()
                .uri("/api/catalog/certifications")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].code").isEqualTo("ORGANIC");
    }

    @Test
    void listRoastLevels_returns200() {
        RoastLevel roast = RoastLevel.builder()
                .id(1).code("LIGHT").name("Ligero").build();
        when(listRoastLevelsUseCase.execute()).thenReturn(Flux.just(roast));

        webTestClient.get()
                .uri("/api/catalog/roast-levels")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].code").isEqualTo("LIGHT");
    }

    @Test
    void listProducts_withFilters_returns200() {
        when(listProductsUseCase.execute(any())).thenReturn(Flux.just(buildProduct()));

        webTestClient.get()
                .uri("/api/catalog/products?search=cafe&region=Huila&minPrice=10000&maxPrice=50000&page=0&size=10")
                .exchange()
                .expectStatus().isOk();
    }
}
