package co.com.marketplace.r2dbc.admin;

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
@Table(schema = "marketplace", name = "admin_activity_log")
public class AdminActivityLogData {
    @Id
    private UUID id;
    @Column("actor_id")
    private UUID actorId;
    @Column("actor_name_snapshot")
    private String actorNameSnapshot;
    private String type;
    private String title;
    private String description;
    private String severity;
    @Column("icon_emoji")
    private String iconEmoji;
    private String metadata;
    @Column("created_at")
    private OffsetDateTime createdAt;
}
