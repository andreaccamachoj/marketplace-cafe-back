package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.ProductStatus;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import co.com.marketplace.model.exception.ForbiddenException;
import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public final class UpdateProductUseCase {

    private final ProductGateway productGateway;
    private final ProducerProfileGateway producerProfileGateway;

    public record Command(
            String name,
            String description,
            BigDecimal price,
            String unit,
            String region,
            String emoji,
            UUID categoryId,
            int stock,
            String status,
            List<String> certificationCodes
    ) {}

    public Mono<Product> execute(UUID productId, UUID userId, Command cmd) {
        return producerProfileGateway.findByUserId(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("PRODUCER_PROFILE_NOT_FOUND",
                        "Perfil de productor no encontrado para usuario: " + userId)))
                .flatMap(profile -> productGateway.findById(productId)
                        .switchIfEmpty(Mono.error(new NotFoundException("PRODUCT_NOT_FOUND", "Product not found: " + productId)))
                        .flatMap(existing -> {
                            if (!profile.getId().equals(existing.getProducerId())) {
                                return Mono.error(new ForbiddenException("FORBIDDEN_UPDATE_PRODUCT", "Not authorized to update this product"));
                            }
                            Product updated = existing.toBuilder()
                                    .name(cmd.name())
                                    .description(cmd.description())
                                    .price(cmd.price())
                                    .unit(cmd.unit())
                                    .region(cmd.region())
                                    .emoji(cmd.emoji())
                                    .categoryId(cmd.categoryId() != null ? cmd.categoryId() : existing.getCategoryId())
                                    .stock(cmd.stock())
                                    .status(cmd.status() != null ? ProductStatus.valueOf(cmd.status()) : existing.getStatus())
                                    .certificationCodes(cmd.certificationCodes())
                                    .updatedAt(OffsetDateTime.now())
                                    .build();
                            return productGateway.update(updated);
                        }));
    }
}
