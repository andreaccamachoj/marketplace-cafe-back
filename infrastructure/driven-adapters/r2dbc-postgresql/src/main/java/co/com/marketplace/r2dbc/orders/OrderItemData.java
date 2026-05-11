package co.com.marketplace.r2dbc.orders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "marketplace", name = "order_items")
public class OrderItemData {
    @Id
    private UUID id;
    @Column("order_id")
    private UUID orderId;
    @Column("product_id")
    private UUID productId;
    @Column("product_name_snapshot")
    private String productNameSnapshot;
    @Column("product_emoji_snapshot")
    private String productEmojiSnapshot;
    private int quantity;
    @Column("unit_price_snapshot")
    private BigDecimal unitPriceSnapshot;
    private BigDecimal subtotal;
}
