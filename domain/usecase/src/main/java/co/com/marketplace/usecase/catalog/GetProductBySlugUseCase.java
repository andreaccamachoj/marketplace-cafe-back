package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import co.com.marketplace.model.exception.NotFoundException;
import reactor.core.publisher.Mono;

import java.util.UUID;

public final class GetProductBySlugUseCase {

    private final ProductGateway productGateway;

    public GetProductBySlugUseCase(ProductGateway productGateway) {
        this.productGateway = productGateway;
    }

    public Mono<Product> execute(UUID productId) {
        return productGateway.findById(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("PRODUCT_NOT_FOUND", "Product not found: " + productId)));
    }
}
