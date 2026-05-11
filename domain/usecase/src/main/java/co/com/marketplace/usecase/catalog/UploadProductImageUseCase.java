package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.ProductImage;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import co.com.marketplace.model.exception.NotFoundException;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

public final class UploadProductImageUseCase {

    private final ProductGateway productGateway;

    public UploadProductImageUseCase(ProductGateway productGateway) {
        this.productGateway = productGateway;
    }

    public Mono<ProductImage> execute(UUID productId, String imageUrl, int displayOrder) {
        return productGateway.findById(productId)
                .switchIfEmpty(Mono.error(new NotFoundException("PRODUCT_NOT_FOUND", "Product not found: " + productId)))
                .map(product -> ProductImage.builder()
                        .id(UUID.randomUUID())
                        .productId(productId)
                        .imageUrl(imageUrl)
                        .displayOrder(displayOrder)
                        .uploadedAt(OffsetDateTime.now())
                        .build());
    }
}
