package co.com.marketplace.usecase.inventory;

import co.com.marketplace.model.inventory.InventoryItem;
import co.com.marketplace.model.inventory.gateways.InventoryGateway;
import reactor.core.publisher.Mono;

import java.util.UUID;

public final class AdjustInventoryUseCase {

    private final InventoryGateway inventoryGateway;

    public AdjustInventoryUseCase(InventoryGateway inventoryGateway) {
        this.inventoryGateway = inventoryGateway;
    }

    public Mono<InventoryItem> execute(UUID productId, int delta) {
        return inventoryGateway.adjust(productId, delta);
    }
}
