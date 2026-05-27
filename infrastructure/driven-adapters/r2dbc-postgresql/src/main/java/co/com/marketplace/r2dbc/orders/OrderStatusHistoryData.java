package co.com.marketplace.r2dbc.orders;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import co.com.marketplace.r2dbc.type.OrderStatusType;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "marketplace", name = "order_status_history")
public class OrderStatusHistoryData {
    @Id
    private UUID id;
    @Column("order_id")
    private UUID orderId;
    private OrderStatusType status;
    @Column("changed_by")
    private UUID changedBy;
    private String notes;
    @Column("changed_at")
    private OffsetDateTime changedAt;
}
