package co.com.marketplace.r2dbc.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "marketplace", name = "cart_items")
public class CartItemData {
    @Id
    private UUID id;
    @Column("cart_id")
    private UUID cartId;
    @Column("product_id")
    private UUID productId;
    private int quantity;
    @Column("unit_price_snapshot")
    private BigDecimal unitPriceSnapshot;
    @Column("added_at")
    private OffsetDateTime addedAt;
}
