package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.ProductStatus;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import co.com.marketplace.model.exception.ForbiddenException;
import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public final class ArchiveProductUseCase {

    private final ProductGateway productGateway;
    private final ProducerProfileGateway producerProfileGateway;

    public Mono<Void> execute(UUID productId, UUID userId) {
        return producerProfileGateway.findByUserId(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("PRODUCER_PROFILE_NOT_FOUND",
                        "Perfil de productor no encontrado para usuario: " + userId)))
                .flatMap(profile -> productGateway.findById(productId)
                        .switchIfEmpty(Mono.error(new NotFoundException("PRODUCT_NOT_FOUND", "Product not found: " + productId)))
                        .flatMap(existing -> {
                            if (!profile.getId().equals(existing.getProducerId())) {
                                return Mono.error(new ForbiddenException("FORBIDDEN_ARCHIVE_PRODUCT", "Not authorized to archive this product"));
                            }
                            return productGateway.updateStatus(productId, ProductStatus.inactive);
                        }));
    }
}
