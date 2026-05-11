package co.com.marketplace.usecase.inventory;

import co.com.marketplace.model.inventory.InventoryItem;
import co.com.marketplace.model.inventory.gateways.InventoryGateway;
import reactor.core.publisher.Mono;

import java.util.UUID;

public final class GetInventoryByProductUseCase {

    private final InventoryGateway inventoryGateway;

    public GetInventoryByProductUseCase(InventoryGateway inventoryGateway) {
        this.inventoryGateway = inventoryGateway;
    }

    public Mono<InventoryItem> execute(UUID productId) {
        return inventoryGateway.findByProductId(productId);
    }
}
