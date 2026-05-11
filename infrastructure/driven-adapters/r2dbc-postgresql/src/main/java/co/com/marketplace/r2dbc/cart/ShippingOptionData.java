package co.com.marketplace.r2dbc.cart;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "marketplace", name = "shipping_options")
public class ShippingOptionData {
    @Id
    private String id;
    private String name;
    @Column("delivery_window")
    private String deliveryWindow;
    private BigDecimal price;
    @Column("is_active")
    private boolean isActive;
    @Column("display_order")
    private int displayOrder;
}
