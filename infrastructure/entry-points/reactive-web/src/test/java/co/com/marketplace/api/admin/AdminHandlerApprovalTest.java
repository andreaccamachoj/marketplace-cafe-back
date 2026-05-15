package co.com.marketplace.api.admin;

import co.com.marketplace.api.config.GlobalErrorWebExceptionHandler;
import co.com.marketplace.api.config.TestTxConfig;
import co.com.marketplace.model.admin.AdminActivityLog;
import co.com.marketplace.model.admin.ProducerApproval;
import co.com.marketplace.model.catalog.Category;
import co.com.marketplace.model.identity.ProducerStatus;
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
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {AdminRouter.class, AdminHandler.class, GlobalErrorWebExceptionHandler.class, TestTxConfig.class})
class AdminHandlerApprovalTest {

    private static final String ADMIN_ID = "550e8400-e29b-41d4-a716-446655440001";

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

    private ProducerApproval buildApproval() {
        return ProducerApproval.builder()
                .id(UUID.randomUUID()).producerId(UUID.randomUUID())
                .producerNameSnapshot("Juan Productor").status(ProducerStatus.pending)
                .submittedAt(OffsetDateTime.now())
                .build();
    }

    private Category buildCategory() {
        return Category.builder()
                .id(UUID.randomUUID()).name("Coffee").slug("coffee")
                .isActive(true).createdAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void approveProducer_returns200() {
        when(approveProducerUseCase.execute(any(), any(), any())).thenReturn(Mono.just(buildApproval()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(ADMIN_ID))
                .patch().uri("/api/admin/producer-approvals/" + UUID.randomUUID() + "/approve")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"notes":"Documentos verificados"}
                        """)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void rejectProducer_returns200() {
        when(rejectProducerUseCase.execute(any(), any(), anyString())).thenReturn(Mono.just(buildApproval()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(ADMIN_ID))
                .patch().uri("/api/admin/producer-approvals/" + UUID.randomUUID() + "/reject")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"reason":"Documentos incompletos"}
                        """)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void updateCategory_returns200() {
        when(updateCategoryUseCase.execute(any(), any(), any(), any(), any(), any(), anyBoolean()))
                .thenReturn(Mono.just(buildCategory()));

        webTestClient.put().uri("/api/admin/categories/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"name":"Coffee","slug":"coffee","description":"desc","parentId":null,"iconEmoji":"☕","isActive":true}
                        """)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void deleteCategory_returns204() {
        when(deleteCategoryUseCase.execute(any())).thenReturn(Mono.empty());

        webTestClient.delete().uri("/api/admin/categories/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void listActivity_returns200() {
        AdminActivityLog log = AdminActivityLog.builder()
                .id(UUID.randomUUID()).actorId(UUID.fromString(ADMIN_ID))
                .type("USER_BANNED").title("Ban").description("desc")
                .severity("high").createdAt(OffsetDateTime.now())
                .build();
        when(listAdminActivityUseCase.execute(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Flux.just(log));

        webTestClient.get().uri("/api/admin/activity")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void listActivity_withFilters_returns200() {
        when(listAdminActivityUseCase.execute(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri("/api/admin/activity?actorId=" + ADMIN_ID + "&action=USER_BANNED&page=0&size=10")
                .exchange()
                .expectStatus().isOk();
    }
}
