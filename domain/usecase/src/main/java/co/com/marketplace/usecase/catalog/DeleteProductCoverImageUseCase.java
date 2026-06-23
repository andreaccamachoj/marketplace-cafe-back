package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.gateways.ImageStorageGateway;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import co.com.marketplace.model.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public final class DeleteProductCoverImageUseCase {

    private final ProductGateway productGateway;
    private final ImageStorageGateway imageStorageGateway;

    public Mono<Product> execute(UUID productId) {
        return productGateway.findById(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("PRODUCT_NOT_FOUND",
                        "Product not found: " + productId)))
                .flatMap(product -> imageStorageGateway.delete(UpdateProductCoverImageUseCase.coverKey(productId))
                        .then(productGateway.updateCoverImage(productId, null)));
    }
}
