package co.com.marketplace.model.inventory;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class InventoryItem {
    UUID id;
    UUID productId;
    int quantity;
    Integer maxStock;
    OffsetDateTime updatedAt;
}
