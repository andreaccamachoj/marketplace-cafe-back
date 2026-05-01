package co.com.marketplace.model.inventory.gateways;

import co.com.marketplace.model.inventory.InventoryItem;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface InventoryGateway {
    Mono<InventoryItem> save(InventoryItem item);
    Mono<InventoryItem> findByProductId(UUID productId);
    Mono<InventoryItem> adjust(UUID productId, int delta);
    Flux<InventoryItem> findByProducerId(UUID producerId);
}
