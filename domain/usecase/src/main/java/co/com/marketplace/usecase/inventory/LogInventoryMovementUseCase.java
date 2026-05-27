package co.com.marketplace.usecase.inventory;

import co.com.marketplace.model.inventory.InventoryItem;
import co.com.marketplace.model.inventory.gateways.InventoryGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public final class LogInventoryMovementUseCase {

    private final InventoryGateway inventoryGateway;

    public Mono<InventoryItem> execute(UUID productId, int delta, String reason) {
        return inventoryGateway.adjust(productId, delta);
    }
}
