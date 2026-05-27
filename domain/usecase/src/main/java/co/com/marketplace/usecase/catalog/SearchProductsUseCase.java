package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import reactor.core.publisher.Flux;

public final class SearchProductsUseCase {

    private final ProductGateway productGateway;

    public SearchProductsUseCase(ProductGateway productGateway) {
        this.productGateway = productGateway;
    }

    public Flux<Product> execute(String query, int page, int size) {
        return productGateway.findAll(query, null, null, null, null, null, null, page, size, null);
    }
}
