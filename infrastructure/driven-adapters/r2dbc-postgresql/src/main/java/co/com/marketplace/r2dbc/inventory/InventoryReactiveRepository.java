package co.com.marketplace.r2dbc.inventory;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface InventoryReactiveRepository extends ReactiveCrudRepository<InventoryData, UUID> {
    Mono<InventoryData> findByProductId(UUID productId);
}
