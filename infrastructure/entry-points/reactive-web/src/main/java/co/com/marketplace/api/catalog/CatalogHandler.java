package co.com.marketplace.api.catalog;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CatalogHandler {

    private final ListProductsUseCase listProductsUseCase;
    private final GetFeaturedProductsUseCase getFeaturedProductsUseCase;
    private final GetProductByIdUseCase getProductByIdUseCase;
    private final GetProductBySlugUseCase getProductBySlugUseCase;
    private final ListCategoriesUseCase listCategoriesUseCase;
    private final GetCategoryBySlugUseCase getCategoryBySlugUseCase;
    private final ListCertificationsUseCase listCertificationsUseCase;
    private final ListRoastLevelsUseCase listRoastLevelsUseCase;
    private final SearchProductsUseCase searchProductsUseCase;
    private final ListProductReviewsUseCase listProductReviewsUseCase;

    public Mono<ServerResponse> listProducts(ServerRequest request) {
        String search     = request.queryParam("search").orElse(null);
        UUID categoryId   = request.queryParam("category").map(UUID::fromString).orElse(null);
        String region     = request.queryParam("region").orElse(null);
        BigDecimal minP   = request.queryParam("minPrice").map(BigDecimal::new).orElse(null);
        BigDecimal maxP   = request.queryParam("maxPrice").map(BigDecimal::new).orElse(null);
        String cert       = request.queryParam("certification").orElse(null);
        String roast      = request.queryParam("roast").orElse(null);
        int page          = request.queryParam("page").map(Integer::parseInt).orElse(0);
        int size          = request.queryParam("size").map(Integer::parseInt).orElse(20);
        String sort       = request.queryParam("sort").orElse("createdAt,desc");

        return listProductsUseCase.execute(
                        new ListProductsUseCase.Command(search, categoryId, region, minP, maxP, cert, roast, page, size, sort))
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> getFeaturedProducts(ServerRequest request) {
        int limit = request.queryParam("limit").map(Integer::parseInt).orElse(8);
        return getFeaturedProductsUseCase.execute(limit)
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> getProductById(ServerRequest request) {
        UUID id = UUID.fromString(request.pathVariable("id"));
        return getProductByIdUseCase.execute(id)
                .flatMap(p -> ServerResponse.ok().bodyValue(p));
    }

    public Mono<ServerResponse> getProductBySlug(ServerRequest request) {
        String slug = request.pathVariable("slug");
        try {
            UUID id = UUID.fromString(slug);
            return getProductBySlugUseCase.execute(id)
                    .flatMap(p -> ServerResponse.ok().bodyValue(p));
        } catch (IllegalArgumentException e) {
            return ServerResponse.notFound().build();
        }
    }

    public Mono<ServerResponse> getProductReviews(ServerRequest request) {
        UUID productId = UUID.fromString(request.pathVariable("id"));
        int page = request.queryParam("page").map(Integer::parseInt).orElse(0);
        int size = request.queryParam("size").map(Integer::parseInt).orElse(20);
        return listProductReviewsUseCase.execute(productId, page, size)
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> listCategories(ServerRequest request) {
        return listCategoriesUseCase.execute()
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> getCategoryBySlug(ServerRequest request) {
        String slug = request.pathVariable("slug");
        return getCategoryBySlugUseCase.execute(slug)
                .flatMap(cat -> ServerResponse.ok().bodyValue(cat));
    }

    public Mono<ServerResponse> listCertifications(ServerRequest request) {
        return listCertificationsUseCase.execute()
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> listRoastLevels(ServerRequest request) {
        return listRoastLevelsUseCase.execute()
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }
}
