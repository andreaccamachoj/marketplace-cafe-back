package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.gateways.ImageStorageGateway;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
public final class UpdateProductCoverImageUseCase {

    private static final long MAX_SIZE_BYTES = 2L * 1024 * 1024;
    private static final Set<String> ALLOWED_CONTENT_TYPES =
            Set.of("image/png", "image/jpeg", "image/jpg");

    private final ProductGateway productGateway;
    private final ImageStorageGateway imageStorageGateway;

    public Mono<Product> execute(UUID productId, byte[] content, String contentType) {
        return validate(content, contentType)
                .then(Mono.defer(() -> productGateway.findById(productId)
                        .switchIfEmpty(Mono.error(new NotFoundException("PRODUCT_NOT_FOUND",
                                "Product not found: " + productId)))))
                .flatMap(product -> imageStorageGateway.upload(content, contentType, coverKey(productId))
                        .flatMap(url -> productGateway.updateCoverImage(productId, url)));
    }

    private Mono<Void> validate(byte[] content, String contentType) {
        if (content == null || content.length == 0) {
            return Mono.error(new ValidationException("IMAGE_EMPTY", "La imagen está vacía"));
        }
        if (content.length > MAX_SIZE_BYTES) {
            return Mono.error(new ValidationException("IMAGE_TOO_LARGE",
                    "La imagen supera el tamaño máximo de 2 MB"));
        }
        String ct = contentType == null ? "" : contentType.toLowerCase();
        if (!ALLOWED_CONTENT_TYPES.contains(ct)) {
            return Mono.error(new ValidationException("INVALID_IMAGE_FORMAT",
                    "Formato no permitido. Use PNG, JPG o JPEG"));
        }
        return Mono.empty();
    }

    static String coverKey(UUID productId) {
        return "products/" + productId + "/cover";
    }
}
