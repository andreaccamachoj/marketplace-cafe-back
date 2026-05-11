package co.com.marketplace.r2dbc.inventory;

import co.com.marketplace.model.inventory.InventoryItem;
import co.com.marketplace.model.inventory.gateways.InventoryGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryRepositoryAdapter implements InventoryGateway {

    private final InventoryReactiveRepository repository;
    private final DatabaseClient databaseClient;

    @Override
    public Mono<InventoryItem> save(InventoryItem item) {
        return repository.save(toData(item))
                .doOnSubscribe(s -> log.debug("[InventoryRepositoryAdapter#save] DB request: productId={}", item.getProductId()))
                .doOnSuccess(r -> log.debug("[InventoryRepositoryAdapter#save] DB response: result={}", r != null))
                .doOnError(e -> log.error("[InventoryRepositoryAdapter#save] DB error: {}", e.getMessage()))
                .map(InventoryRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<InventoryItem> findByProductId(UUID productId) {
        return repository.findByProductId(productId)
                .doOnSubscribe(s -> log.debug("[InventoryRepositoryAdapter#findByProductId] DB request: productId={}", productId))
                .doOnSuccess(r -> log.debug("[InventoryRepositoryAdapter#findByProductId] DB response: found={}", r != null))
                .doOnError(e -> log.error("[InventoryRepositoryAdapter#findByProductId] DB error: {}", e.getMessage()))
                .map(InventoryRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<InventoryItem> adjust(UUID productId, int delta) {
        return databaseClient.sql(
                "UPDATE marketplace.inventory SET quantity = quantity + :delta, updated_at = :now " +
                "WHERE product_id = :productId RETURNING *"
        )
                .bind("delta", delta)
                .bind("now", OffsetDateTime.now())
                .bind("productId", productId)
                .map((row, meta) -> InventoryData.builder()
                        .id(row.get("id", UUID.class))
                        .productId(row.get("product_id", UUID.class))
                        .quantity(row.get("quantity", Integer.class))
                        .maxStock(row.get("max_stock", Integer.class))
                        .updatedAt(row.get("updated_at", OffsetDateTime.class))
                        .build())
                .one()
                .doOnSubscribe(s -> log.debug("[InventoryRepositoryAdapter#adjust] DB request: productId={}, delta={}", productId, delta))
                .doOnSuccess(r -> log.debug("[InventoryRepositoryAdapter#adjust] DB response: result={}", r != null))
                .doOnError(e -> log.error("[InventoryRepositoryAdapter#adjust] DB error: {}", e.getMessage()))
                .map(InventoryRepositoryAdapter::toDomain);
    }

    @Override
    public Flux<InventoryItem> findByProducerId(UUID producerId) {
        return databaseClient.sql(
                "SELECT i.* FROM marketplace.inventory i " +
                "JOIN marketplace.products p ON p.id = i.product_id " +
                "WHERE p.producer_id = :producerId"
        )
                .bind("producerId", producerId)
                .map((row, meta) -> InventoryData.builder()
                        .id(row.get("id", UUID.class))
                        .productId(row.get("product_id", UUID.class))
                        .quantity(row.get("quantity", Integer.class))
                        .maxStock(row.get("max_stock", Integer.class))
                        .updatedAt(row.get("updated_at", OffsetDateTime.class))
                        .build())
                .all()
                .doOnSubscribe(s -> log.debug("[InventoryRepositoryAdapter#findByProducerId] DB request: producerId={}", producerId))
                .doOnComplete(() -> log.debug("[InventoryRepositoryAdapter#findByProducerId] DB response: complete"))
                .doOnError(e -> log.error("[InventoryRepositoryAdapter#findByProducerId] DB error: {}", e.getMessage()))
                .map(InventoryRepositoryAdapter::toDomain);
    }

    static InventoryItem toDomain(InventoryData d) {
        return InventoryItem.builder()
                .id(d.getId())
                .productId(d.getProductId())
                .quantity(d.getQuantity())
                .maxStock(d.getMaxStock())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    static InventoryData toData(InventoryItem item) {
        return InventoryData.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .quantity(item.getQuantity())
                .maxStock(item.getMaxStock())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
