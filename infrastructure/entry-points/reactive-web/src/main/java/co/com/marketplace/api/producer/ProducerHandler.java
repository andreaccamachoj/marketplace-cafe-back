package co.com.marketplace.api.producer;

import co.com.marketplace.model.catalog.ProductStatus;
import co.com.marketplace.model.orders.OrderStatus;
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
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProducerHandler {

    private final ListMyProductsUseCase listMyProductsUseCase;
    private final CreateProductUseCase createProductUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final ArchiveProductUseCase archiveProductUseCase;
    private final GetProductsByProducerUseCase getProductsByProducerUseCase;
    private final ListProducerOrdersUseCase listProducerOrdersUseCase;
    private final UpdateOrderStatusUseCase updateOrderStatusUseCase;
    private final ConfirmOrderPaymentUseCase confirmOrderPaymentUseCase;
    private final GetFarmProfileUseCase getFarmProfileUseCase;
    private final UpdateFarmProfileUseCase updateFarmProfileUseCase;
    private final ListProducerReviewsUseCase listProducerReviewsUseCase;
    private final GetInventoryByProductUseCase getInventoryByProductUseCase;
    private final AdjustInventoryUseCase adjustInventoryUseCase;
    private final AddFarmCertificationUseCase addFarmCertificationUseCase;
    private final GetFarmCertificationsUseCase getFarmCertificationsUseCase;
    private final RemoveFarmCertificationUseCase removeFarmCertificationUseCase;

    record CreateProductRequest(UUID categoryId, String name, String description,
                                BigDecimal price, String unit, String region, String emoji) {}
    record UpdateProductRequest(String name, String description, BigDecimal price,
                                String unit, String region, String emoji, UUID categoryId) {}
    record UpdateOrderStatusRequest(String newStatus, String note) {}
    record ConfirmPaymentRequest(boolean verified, String note) {}
    record FarmRequest(String name, String municipality, String department,
                       BigDecimal altitudeMasl, BigDecimal areaHectares,
                       String mainVariety, String process, String description) {}
    record InventoryAdjustRequest(UUID productId, int delta) {}
    record CertRequest(String type, String name, String issuer, String expiryDate) {}

    public Mono<ServerResponse> listMyProducts(ServerRequest request) {
        ProductStatus status = request.queryParam("status").map(ProductStatus::valueOf).orElse(null);
        int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = request.queryParam("size").map(Integer::parseInt).orElse(20);
        return userId(request)
                .flatMapMany(uid -> listMyProductsUseCase.execute(uid, status, page, size))
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> createProduct(ServerRequest request) {
        return userId(request)
                .flatMap(uid -> request.bodyToMono(CreateProductRequest.class)
                        .flatMap(body -> createProductUseCase.execute(
                                new CreateProductUseCase.Command(uid, body.categoryId(), body.name(),
                                        body.description(), body.price(), body.unit(), body.region(), body.emoji()))))
                .flatMap(product -> ServerResponse.status(HttpStatus.CREATED).bodyValue(product));
    }

    public Mono<ServerResponse> updateProduct(ServerRequest request) {
        UUID productId = UUID.fromString(request.pathVariable("id"));
        return userId(request)
                .flatMap(uid -> request.bodyToMono(UpdateProductRequest.class)
                        .flatMap(body -> updateProductUseCase.execute(productId, uid,
                                new UpdateProductUseCase.Command(body.name(), body.description(),
                                        body.price(), body.unit(), body.region(), body.emoji(), body.categoryId()))))
                .flatMap(product -> ServerResponse.ok().bodyValue(product));
    }

    public Mono<ServerResponse> archiveProduct(ServerRequest request) {
        UUID productId = UUID.fromString(request.pathVariable("id"));
        return userId(request)
                .flatMap(uid -> archiveProductUseCase.execute(productId, uid))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> listOrders(ServerRequest request) {
        OrderStatus status = request.queryParam("status").map(OrderStatus::valueOf).orElse(null);
        int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = request.queryParam("size").map(Integer::parseInt).orElse(20);
        return userId(request)
                .flatMapMany(uid -> listProducerOrdersUseCase.execute(uid, status, page, size))
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> updateOrderStatus(ServerRequest request) {
        UUID orderId = UUID.fromString(request.pathVariable("id"));
        return userId(request)
                .flatMap(uid -> request.bodyToMono(UpdateOrderStatusRequest.class)
                        .flatMap(body -> updateOrderStatusUseCase.execute(
                                orderId, uid, OrderStatus.valueOf(body.newStatus()), body.note())))
                .flatMap(order -> ServerResponse.ok().bodyValue(order));
    }

    public Mono<ServerResponse> confirmPayment(ServerRequest request) {
        UUID orderId = UUID.fromString(request.pathVariable("id"));
        return userId(request)
                .flatMap(uid -> request.bodyToMono(ConfirmPaymentRequest.class)
                        .flatMap(body -> confirmOrderPaymentUseCase.execute(orderId, uid, body.verified(), body.note())))
                .flatMap(payment -> ServerResponse.ok().bodyValue(payment));
    }

    public Mono<ServerResponse> getFarm(ServerRequest request) {
        return userId(request)
                .flatMap(getFarmProfileUseCase::execute)
                .flatMap(farm -> ServerResponse.ok().bodyValue(farm));
    }

    public Mono<ServerResponse> updateFarm(ServerRequest request) {
        return userId(request)
                .flatMap(uid -> request.bodyToMono(FarmRequest.class)
                        .flatMap(body -> updateFarmProfileUseCase.execute(uid,
                                new UpdateFarmProfileUseCase.Command(body.name(), body.municipality(),
                                        body.department(), body.altitudeMasl(), body.areaHectares(),
                                        body.mainVariety(), body.process(), body.description()))))
                .flatMap(farm -> ServerResponse.ok().bodyValue(farm));
    }

    public Mono<ServerResponse> listReviews(ServerRequest request) {
        int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = request.queryParam("size").map(Integer::parseInt).orElse(20);
        return userId(request)
                .flatMapMany(uid -> listProducerReviewsUseCase.execute(uid, page, size))
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> getInventory(ServerRequest request) {
        int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = request.queryParam("size").map(Integer::parseInt).orElse(100);
        return request.queryParam("productId")
                .map(id -> getInventoryByProductUseCase.execute(UUID.fromString(id))
                        .flatMap(item -> ServerResponse.ok().bodyValue(item)))
                .orElseGet(() -> userId(request)
                        .flatMapMany(uid -> getProductsByProducerUseCase.execute(uid, page, size))
                        .collectList()
                        .flatMap(list -> ServerResponse.ok().bodyValue(list)));
    }

    public Mono<ServerResponse> adjustInventory(ServerRequest request) {
        return request.bodyToMono(InventoryAdjustRequest.class)
                .flatMap(body -> adjustInventoryUseCase.execute(body.productId(), body.delta()))
                .flatMap(item -> ServerResponse.ok().bodyValue(item));
    }

    public Mono<ServerResponse> getFarmCertifications(ServerRequest request) {
        return userId(request)
                .flatMapMany(uid -> getFarmCertificationsUseCase.execute(uid))
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> addFarmCertification(ServerRequest request) {
        return userId(request)
                .flatMap(uid -> request.bodyToMono(CertRequest.class)
                        .flatMap(body -> addFarmCertificationUseCase.execute(uid,
                                new AddFarmCertificationUseCase.Command(
                                        body.type(), body.name(), body.issuer(),
                                        body.expiryDate() != null
                                                ? java.time.LocalDate.parse(body.expiryDate())
                                                : null))))
                .flatMap(cert -> ServerResponse.status(org.springframework.http.HttpStatus.CREATED).bodyValue(cert));
    }

    public Mono<ServerResponse> removeFarmCertification(ServerRequest request) {
        UUID certId = UUID.fromString(request.pathVariable("id"));
        return removeFarmCertificationUseCase.execute(certId)
                .then(ServerResponse.noContent().build());
    }

    private Mono<UUID> userId(ServerRequest request) {
        return request.principal().map(p -> UUID.fromString(p.getName()));
    }
}
