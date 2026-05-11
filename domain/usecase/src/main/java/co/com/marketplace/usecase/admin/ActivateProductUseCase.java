package co.com.marketplace.usecase.admin;

import co.com.marketplace.model.catalog.ProductStatus;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import co.com.marketplace.model.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public final class ActivateProductUseCase {
    private final ProductGateway productGateway;

    public Mono<Void> execute(UUID productId) {
        return productGateway.findById(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("PRODUCT_NOT_FOUND", "Product not found: " + productId)))
                .flatMap(p -> productGateway.updateStatus(productId, ProductStatus.active));
    }
}
