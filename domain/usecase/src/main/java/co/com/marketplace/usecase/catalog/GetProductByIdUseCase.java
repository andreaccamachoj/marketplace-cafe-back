package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import co.com.marketplace.model.exception.NotFoundException;
import reactor.core.publisher.Mono;

import java.util.UUID;

public final class GetProductByIdUseCase {

    private final ProductGateway productGateway;

    public GetProductByIdUseCase(ProductGateway productGateway) {
        this.productGateway = productGateway;
    }

    public Mono<Product> execute(UUID id) {
        return productGateway.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("PRODUCT_NOT_FOUND", "Product not found: " + id)));
    }
}
