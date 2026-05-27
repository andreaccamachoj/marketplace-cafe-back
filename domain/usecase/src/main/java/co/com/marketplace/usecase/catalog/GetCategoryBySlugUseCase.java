package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.Category;
import co.com.marketplace.model.catalog.gateways.CategoryGateway;
import co.com.marketplace.model.exception.NotFoundException;
import reactor.core.publisher.Mono;

public final class GetCategoryBySlugUseCase {

    private final CategoryGateway categoryGateway;

    public GetCategoryBySlugUseCase(CategoryGateway categoryGateway) {
        this.categoryGateway = categoryGateway;
    }

    public Mono<Category> execute(String slug) {
        return categoryGateway.findBySlug(slug)
                .switchIfEmpty(Mono.error(new NotFoundException("CATEGORY_NOT_FOUND", "Category not found: " + slug)));
    }
}
