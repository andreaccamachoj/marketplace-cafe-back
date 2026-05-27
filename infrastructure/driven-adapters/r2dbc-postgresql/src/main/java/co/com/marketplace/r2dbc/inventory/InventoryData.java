package co.com.marketplace.r2dbc.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "marketplace", name = "inventory")
public class InventoryData {
    @Id
    private UUID id;
    private UUID productId;
    private int quantity;
    private Integer maxStock;
    private OffsetDateTime updatedAt;
}
