package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.Category;
import co.com.marketplace.model.catalog.gateways.CategoryGateway;
import reactor.core.publisher.Flux;

public final class ListCategoriesUseCase {

    private final CategoryGateway categoryGateway;

    public ListCategoriesUseCase(CategoryGateway categoryGateway) {
        this.categoryGateway = categoryGateway;
    }

    public Flux<Category> execute() {
        return categoryGateway.findAll();
    }
}
