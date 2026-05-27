package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import reactor.core.publisher.Flux;

public final class GetFeaturedProductsUseCase {

    private final ProductGateway productGateway;

    public GetFeaturedProductsUseCase(ProductGateway productGateway) {
        this.productGateway = productGateway;
    }

    public Flux<Product> execute(int limit) {
        return productGateway.findFeatured(limit);
    }
}
