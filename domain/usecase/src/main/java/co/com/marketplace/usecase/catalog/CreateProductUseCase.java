package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.ProductStatus;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
import co.com.marketplace.model.inventory.InventoryItem;
import co.com.marketplace.model.inventory.gateways.InventoryGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public final class CreateProductUseCase {

    private final ProductGateway productGateway;
    private final InventoryGateway inventoryGateway;
    private final ProducerProfileGateway producerProfileGateway;

    public record Command(
            UUID userId,
            UUID categoryId,
            String name,
            String description,
            BigDecimal price,
            String unit,
            String region,
            String emoji,
            int stock,
            String status,
            List<String> certificationCodes
    ) {}

    public Mono<Product> execute(Command cmd) {
        return producerProfileGateway.findByUserId(cmd.userId())
                .switchIfEmpty(Mono.error(new NotFoundException("PRODUCER_PROFILE_NOT_FOUND",
                        "Perfil de productor no encontrado para usuario: " + cmd.userId())))
                .flatMap(profile -> {
                    Product product = Product.builder()
                            .id(UUID.randomUUID())
                            .producerId(profile.getId())
                            .categoryId(cmd.categoryId())
                            .name(cmd.name())
                            .description(cmd.description())
                            .price(cmd.price())
                            .unit(cmd.unit())
                            .region(cmd.region())
                            .emoji(cmd.emoji())
                            .status(cmd.status() != null ? ProductStatus.valueOf(cmd.status()) : ProductStatus.draft)
                            .certificationCodes(cmd.certificationCodes())
                            .soldCount(0)
                            .createdAt(OffsetDateTime.now())
                            .updatedAt(OffsetDateTime.now())
                            .build();
                    return productGateway.save(product)
                            .flatMap(saved -> {
                                InventoryItem item = InventoryItem.builder()
                                        .id(UUID.randomUUID())
                                        .productId(saved.getId())
                                        .quantity(cmd.stock())
                                        .updatedAt(OffsetDateTime.now())
                                        .build();
                                return inventoryGateway.save(item)
                                        .thenReturn(saved.toBuilder().stock(cmd.stock()).build());
                            });
                });
    }
}
