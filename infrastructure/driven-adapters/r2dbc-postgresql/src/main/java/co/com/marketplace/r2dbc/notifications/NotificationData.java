package co.com.marketplace.r2dbc.notifications;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "marketplace", name = "notifications")
public class NotificationData {
    @Id
    private UUID id;
    @Column("user_id")
    private UUID userId;
    private String type;
    private String message;
    @Column("is_read")
    private boolean isRead;
    private String metadata;
    @Column("created_at")
    private OffsetDateTime createdAt;
}
