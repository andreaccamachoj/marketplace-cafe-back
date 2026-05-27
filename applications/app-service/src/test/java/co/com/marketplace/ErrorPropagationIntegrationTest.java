package co.com.marketplace;

import co.com.marketplace.api.catalog.CatalogHandler;
import co.com.marketplace.api.catalog.CatalogRouter;
import co.com.marketplace.api.config.GlobalErrorWebExceptionHandler;
import co.com.marketplace.api.config.SecurityConfig;
import co.com.marketplace.api.identity.AuthHandler;
import co.com.marketplace.api.identity.AuthRouter;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import co.com.marketplace.model.exception.ConflictException;
import co.com.marketplace.model.exception.ValidationException;
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
import co.com.marketplace.usecase.identity.ChangePasswordUseCase;
import co.com.marketplace.usecase.identity.ConfirmPasswordResetUseCase;
import co.com.marketplace.usecase.identity.GetCurrentUserUseCase;
import co.com.marketplace.usecase.identity.LoginUseCase;
import co.com.marketplace.usecase.identity.LogoutUseCase;
import co.com.marketplace.usecase.identity.RecordPrivacyConsentUseCase;
import co.com.marketplace.usecase.identity.RefreshTokenUseCase;
import co.com.marketplace.usecase.identity.RegisterBuyerUseCase;
import co.com.marketplace.usecase.identity.RegisterProducerUseCase;
import co.com.marketplace.usecase.identity.RequestPasswordResetUseCase;
import co.com.marketplace.usecase.reviews.ListProductReviewsUseCase;
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

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {
        AuthRouter.class, AuthHandler.class,
        CatalogRouter.class, CatalogHandler.class,
        SecurityConfig.class,
        GlobalErrorWebExceptionHandler.class,
        ErrorPropagationIntegrationTest.TestUseCasesConfig.class
})
class ErrorPropagationIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean private TokenProviderGateway tokenProviderGateway;
    @MockitoBean private ProductGateway productGateway;
    @MockitoBean private TransactionalOperator tx;

    // AuthHandler dependencies
    @MockitoBean private RegisterBuyerUseCase registerBuyerUseCase;
    @MockitoBean private RegisterProducerUseCase registerProducerUseCase;
    @MockitoBean private LoginUseCase loginUseCase;
    @MockitoBean private RefreshTokenUseCase refreshTokenUseCase;
    @MockitoBean private LogoutUseCase logoutUseCase;
    @MockitoBean private RequestPasswordResetUseCase requestPasswordResetUseCase;
    @MockitoBean private ConfirmPasswordResetUseCase confirmPasswordResetUseCase;
    @MockitoBean private GetCurrentUserUseCase getCurrentUserUseCase;
    @MockitoBean private ChangePasswordUseCase changePasswordUseCase;
    @MockitoBean private RecordPrivacyConsentUseCase recordPrivacyConsentUseCase;

    // CatalogHandler dependencies (all mocked except GetProductByIdUseCase)
    @MockitoBean private ListProductsUseCase listProductsUseCase;
    @MockitoBean private GetFeaturedProductsUseCase getFeaturedProductsUseCase;
    @MockitoBean private GetProductBySlugUseCase getProductBySlugUseCase;
    @MockitoBean private ListCategoriesUseCase listCategoriesUseCase;
    @MockitoBean private GetCategoryBySlugUseCase getCategoryBySlugUseCase;
    @MockitoBean private ListCertificationsUseCase listCertificationsUseCase;
    @MockitoBean private ListRoastLevelsUseCase listRoastLevelsUseCase;
    @MockitoBean private SearchProductsUseCase searchProductsUseCase;
    @MockitoBean private ListProductReviewsUseCase listProductReviewsUseCase;

    @BeforeEach
    void setupTx() {
        lenient().when(tx.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @TestConfiguration
    static class TestUseCasesConfig {
        @Bean
        GetProductByIdUseCase getProductByIdUseCase(ProductGateway pg) {
            return new GetProductByIdUseCase(pg);
        }
    }

    // ── Scenarios ────────────────────────────────────────────────────────────

    @Test
    void protectedRoute_withoutToken_returns401() {
        // /api/cart is not a public route — security blocks unauthenticated requests
        webTestClient.get().uri("/api/cart")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void adminRoute_withBuyerRole_returns403() {
        String token = "buyer-token";
        UUID userId = UUID.randomUUID();
        when(tokenProviderGateway.isTokenValid(token)).thenReturn(true);
        when(tokenProviderGateway.validateToken(token)).thenReturn(Mono.just(userId));
        when(tokenProviderGateway.extractRole(token)).thenReturn("BUYER");

        // /api/admin/** requires ROLE_ADMIN — BUYER is blocked with 403
        webTestClient.get().uri("/api/admin/users")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void getProduct_nonExistentId_returns404() {
        UUID id = UUID.randomUUID();
        when(productGateway.findById(id)).thenReturn(Mono.empty());

        webTestClient.get().uri("/api/catalog/products/" + id)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void registerBuyer_duplicateEmail_returns409() {
        when(registerBuyerUseCase.execute(any()))
                .thenReturn(Mono.error(new ConflictException("EMAIL_ALREADY_EXISTS",
                        "El email ya está registrado")));

        webTestClient.post().uri("/api/auth/register/buyer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", "dup@test.com", "password", "pass123",
                        "fullName", "Test", "phone", "555"))
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    void registerBuyer_invalidData_returns400() {
        when(registerBuyerUseCase.execute(any()))
                .thenReturn(Mono.error(new ValidationException("INVALID_DATA",
                        "Los datos proporcionados son inválidos")));

        webTestClient.post().uri("/api/auth/register/buyer")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("email", "", "password", "", "fullName", "", "phone", ""))
                .exchange()
                .expectStatus().isBadRequest();
    }
}
