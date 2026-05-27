package co.com.marketplace.api.admin;

import co.com.marketplace.api.config.GlobalErrorWebExceptionHandler;
import co.com.marketplace.api.config.TestTxConfig;
import co.com.marketplace.model.catalog.Category;
import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.ProductStatus;
import co.com.marketplace.model.identity.User;
import co.com.marketplace.model.identity.UserStatus;
import co.com.marketplace.usecase.admin.ActivateProductUseCase;
import co.com.marketplace.usecase.admin.ApproveProducerUseCase;
import co.com.marketplace.usecase.admin.BanUserUseCase;
import co.com.marketplace.usecase.admin.CreateCategoryUseCase;
import co.com.marketplace.usecase.admin.DeleteCategoryUseCase;
import co.com.marketplace.usecase.admin.ListAdminActivityUseCase;
import co.com.marketplace.usecase.admin.ListAllProductsUseCase;
import co.com.marketplace.usecase.admin.ListPendingApprovalsUseCase;
import co.com.marketplace.usecase.admin.ListUsersUseCase;
import co.com.marketplace.usecase.admin.RejectProducerUseCase;
import co.com.marketplace.usecase.admin.UnbanUserUseCase;
import co.com.marketplace.usecase.admin.UpdateCategoryUseCase;
import co.com.marketplace.usecase.catalog.ListCategoriesUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
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
@ContextConfiguration(classes = {AdminRouter.class, AdminHandler.class, GlobalErrorWebExceptionHandler.class, TestTxConfig.class})
class AdminHandlerTest {

    @Autowired private WebTestClient webTestClient;

    @MockitoBean private ListUsersUseCase listUsersUseCase;
    @MockitoBean private BanUserUseCase banUserUseCase;
    @MockitoBean private UnbanUserUseCase unbanUserUseCase;
    @MockitoBean private ListPendingApprovalsUseCase listPendingApprovalsUseCase;
    @MockitoBean private ApproveProducerUseCase approveProducerUseCase;
    @MockitoBean private RejectProducerUseCase rejectProducerUseCase;
    @MockitoBean private CreateCategoryUseCase createCategoryUseCase;
    @MockitoBean private UpdateCategoryUseCase updateCategoryUseCase;
    @MockitoBean private DeleteCategoryUseCase deleteCategoryUseCase;
    @MockitoBean private ListAdminActivityUseCase listAdminActivityUseCase;
    @MockitoBean private ListCategoriesUseCase listCategoriesUseCase;
    @MockitoBean private ListAllProductsUseCase listAllProductsUseCase;
    @MockitoBean private ActivateProductUseCase activateProductUseCase;

    private User buildUser() {
        return User.builder()
                .id(UUID.randomUUID()).email("user@test.com").fullName("Test User")
                .status(UserStatus.active).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();
    }

    private Category buildCategory() {
        return Category.builder()
                .id(UUID.randomUUID()).name("Coffee").slug("coffee")
                .isActive(true).createdAt(OffsetDateTime.now())
                .build();
    }

    private Product buildProduct() {
        return Product.builder()
                .id(UUID.randomUUID()).name("Test Coffee")
                .price(BigDecimal.valueOf(20)).status(ProductStatus.active)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void listUsers_returns200() {
        when(listUsersUseCase.execute(any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Flux.just(buildUser()));

        webTestClient.get().uri("/api/admin/users")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void banUser_returns200() {
        when(banUserUseCase.execute(any(), anyString())).thenReturn(Mono.just(buildUser()));

        webTestClient.patch().uri("/api/admin/users/" + UUID.randomUUID() + "/ban")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"reason":"violation"}
                        """)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void unbanUser_returns200() {
        when(unbanUserUseCase.execute(any())).thenReturn(Mono.just(buildUser()));

        webTestClient.patch().uri("/api/admin/users/" + UUID.randomUUID() + "/unban")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void listApprovals_returns200() {
        when(listPendingApprovalsUseCase.executeAll(anyInt(), anyInt())).thenReturn(Flux.empty());

        webTestClient.get().uri("/api/admin/producer-approvals")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void listAdminProducts_returns200() {
        when(listAllProductsUseCase.execute(anyInt(), anyInt())).thenReturn(Flux.just(buildProduct()));

        webTestClient.get().uri("/api/admin/products")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void activateProduct_returns204() {
        when(activateProductUseCase.execute(any())).thenReturn(Mono.empty());

        webTestClient.patch().uri("/api/admin/products/" + UUID.randomUUID() + "/activate")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void createCategory_returns201() {
        when(createCategoryUseCase.execute(any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(buildCategory()));

        webTestClient.post().uri("/api/admin/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name":"Coffee","slug":"coffee","description":"desc","parentId":null,"iconEmoji":"","isActive":true}
                        """)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void listAdminCategories_returns200() {
        when(listCategoriesUseCase.execute()).thenReturn(Flux.just(buildCategory()));

        webTestClient.get().uri("/api/admin/categories")
                .exchange()
                .expectStatus().isOk();
    }
}
