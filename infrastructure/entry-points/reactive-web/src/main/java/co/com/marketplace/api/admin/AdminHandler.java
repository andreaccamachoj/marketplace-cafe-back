package co.com.marketplace.api.admin;

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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AdminHandler {

    private final ListUsersUseCase listUsersUseCase;
    private final BanUserUseCase banUserUseCase;
    private final UnbanUserUseCase unbanUserUseCase;
    private final ListPendingApprovalsUseCase listPendingApprovalsUseCase;
    private final ApproveProducerUseCase approveProducerUseCase;
    private final RejectProducerUseCase rejectProducerUseCase;
    private final CreateCategoryUseCase createCategoryUseCase;
    private final UpdateCategoryUseCase updateCategoryUseCase;
    private final DeleteCategoryUseCase deleteCategoryUseCase;
    private final ListAdminActivityUseCase listAdminActivityUseCase;
    private final ListCategoriesUseCase listCategoriesUseCase;
    private final ListAllProductsUseCase listAllProductsUseCase;
    private final ActivateProductUseCase activateProductUseCase;
    private final TransactionalOperator tx;

    record BanRequest(String reason) {}
    record ApproveRequest(String notes) {}
    record RejectRequest(String reason) {}
    record CategoryRequest(String name, String slug, String description,
                           UUID parentId, String iconEmoji, boolean isActive) {}

    public Mono<ServerResponse> listUsers(ServerRequest request) {
        String role   = request.queryParam("role").orElse(null);
        UserStatus st = request.queryParam("status").map(UserStatus::valueOf).orElse(null);
        String search = request.queryParam("search").orElse(null);
        int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = request.queryParam("size").map(Integer::parseInt).orElse(20);
        return listUsersUseCase.execute(role, st, search, page, size)
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> banUser(ServerRequest request) {
        UUID userId = UUID.fromString(request.pathVariable("id"));
        return request.bodyToMono(BanRequest.class)
                .flatMap(body -> banUserUseCase.execute(userId, body.reason()))
                .flatMap(user -> ServerResponse.ok().bodyValue(user));
    }

    public Mono<ServerResponse> unbanUser(ServerRequest request) {
        UUID userId = UUID.fromString(request.pathVariable("id"));
        return unbanUserUseCase.execute(userId)
                .flatMap(user -> ServerResponse.ok().bodyValue(user));
    }

    public Mono<ServerResponse> listApprovals(ServerRequest request) {
        int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = request.queryParam("size").map(Integer::parseInt).orElse(100);
        return listPendingApprovalsUseCase.executeAll(page, size)
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> listAdminCategories(ServerRequest request) {
        return listCategoriesUseCase.execute()
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> listAdminProducts(ServerRequest request) {
        int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = request.queryParam("size").map(Integer::parseInt).orElse(50);
        return listAllProductsUseCase.execute(page, size)
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> activateProduct(ServerRequest request) {
        UUID productId = UUID.fromString(request.pathVariable("id"));
        return activateProductUseCase.execute(productId)
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> approveProducer(ServerRequest request) {
        UUID approvalId = UUID.fromString(request.pathVariable("id"));
        return userId(request)
                .flatMap(uid -> request.bodyToMono(ApproveRequest.class)
                        .flatMap(body -> approveProducerUseCase.execute(approvalId, uid, body.notes())))
                .flatMap(approval -> ServerResponse.ok().bodyValue(approval));
    }

    public Mono<ServerResponse> rejectProducer(ServerRequest request) {
        UUID approvalId = UUID.fromString(request.pathVariable("id"));
        return userId(request)
                .flatMap(uid -> request.bodyToMono(RejectRequest.class)
                        .flatMap(body -> rejectProducerUseCase.execute(approvalId, uid, body.reason())))
                .flatMap(approval -> ServerResponse.ok().bodyValue(approval));
    }

    public Mono<ServerResponse> createCategory(ServerRequest request) {
        return request.bodyToMono(CategoryRequest.class)
                .flatMap(body -> createCategoryUseCase.execute(
                        body.name(), body.slug(), body.description(), body.parentId(), body.iconEmoji())
                        .as(tx::transactional))
                .flatMap(cat -> ServerResponse.status(HttpStatus.CREATED).bodyValue(cat));
    }

    public Mono<ServerResponse> updateCategory(ServerRequest request) {
        UUID catId = UUID.fromString(request.pathVariable("id"));
        return request.bodyToMono(CategoryRequest.class)
                .flatMap(body -> updateCategoryUseCase.execute(
                        catId, body.name(), body.slug(), body.description(),
                        body.parentId(), body.iconEmoji(), body.isActive()))
                .flatMap(cat -> ServerResponse.ok().bodyValue(cat));
    }

    public Mono<ServerResponse> deleteCategory(ServerRequest request) {
        UUID catId = UUID.fromString(request.pathVariable("id"));
        return deleteCategoryUseCase.execute(catId)
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> listActivity(ServerRequest request) {
        UUID actorId = request.queryParam("actorId").map(UUID::fromString).orElse(null);
        String action = request.queryParam("action").orElse(null);
        OffsetDateTime from = request.queryParam("from").map(OffsetDateTime::parse).orElse(null);
        OffsetDateTime to = request.queryParam("to").map(OffsetDateTime::parse).orElse(null);
        int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = request.queryParam("size").map(Integer::parseInt).orElse(20);
        return listAdminActivityUseCase.execute(actorId, action, from, to, page, size)
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    private Mono<UUID> userId(ServerRequest request) {
        return request.principal().map(p -> UUID.fromString(p.getName()));
    }
}
