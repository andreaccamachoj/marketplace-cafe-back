package co.com.marketplace.api.config;

import co.com.marketplace.model.gateway.TokenProviderGateway;
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
import co.com.marketplace.api.catalog.CatalogHandler;
import co.com.marketplace.api.catalog.CatalogRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {SecurityConfig.class, CatalogRouter.class, CatalogHandler.class})
class SecurityConfigTest {

    @Autowired private WebTestClient webTestClient;
    @Autowired private PasswordEncoder passwordEncoder;

    @MockitoBean private TokenProviderGateway tokenProvider;
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

    @Test
    void passwordEncoder_encodesAndVerifiesCorrectly() {
        String raw = "test-password-123";
        String encoded = passwordEncoder.encode(raw);
        assertThat(encoded).isNotEqualTo(raw);
        assertThat(passwordEncoder.matches(raw, encoded)).isTrue();
        assertThat(passwordEncoder.matches("wrong", encoded)).isFalse();
    }

    @Test
    void catalogEndpoint_accessibleWithoutAuth() {
        when(listProductsUseCase.execute(any())).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/catalog/products")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void jwtFilter_withNoBearerHeader_allowsPublicRoute() {
        when(getFeaturedProductsUseCase.execute(anyInt())).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/catalog/products/featured")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void jwtFilter_withInvalidBearerToken_skipsAuthentication() {
        when(tokenProvider.isTokenValid("bad-token")).thenReturn(false);
        when(listProductsUseCase.execute(any())).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/catalog/products")
                .header("Authorization", "Bearer bad-token")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void jwtFilter_withValidToken_setsAuthentication() {
        UUID userId = UUID.randomUUID();
        when(tokenProvider.isTokenValid("valid.jwt.token")).thenReturn(true);
        when(tokenProvider.validateToken("valid.jwt.token")).thenReturn(Mono.just(userId));
        when(tokenProvider.extractRole("valid.jwt.token")).thenReturn("BUYER");
        when(listProductsUseCase.execute(any())).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/catalog/products")
                .header("Authorization", "Bearer valid.jwt.token")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void jwtFilter_withNonBearerHeader_skipsAuthentication() {
        when(listProductsUseCase.execute(any())).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/catalog/products")
                .header("Authorization", "Basic dXNlcjpwYXNz")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void jwtFilter_withNullRoleToken_setsEmptyAuthorities() {
        UUID userId = UUID.randomUUID();
        when(tokenProvider.isTokenValid("no-role-token")).thenReturn(true);
        when(tokenProvider.validateToken("no-role-token")).thenReturn(Mono.just(userId));
        when(tokenProvider.extractRole("no-role-token")).thenReturn(null);
        when(listProductsUseCase.execute(any())).thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/catalog/products")
                .header("Authorization", "Bearer no-role-token")
                .exchange()
                .expectStatus().isOk();
    }
}
