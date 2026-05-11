package co.com.marketplace.r2dbc.identity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import co.com.marketplace.r2dbc.type.ProducerStatusType;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "marketplace", name = "producer_profiles")
public class ProducerProfileData {
    @Id
    private UUID id;
    @Column("user_id")
    private UUID userId;
    private String bio;
    private String city;
    private String department;
    private ProducerStatusType status;
    @Column("rejection_reason")
    private String rejectionReason;
    @Column("approved_by")
    private UUID approvedBy;
    @Column("approved_at")
    private OffsetDateTime approvedAt;
    @Column("avatar_initials")
    private String avatarInitials;
    @Column("created_at")
    private OffsetDateTime createdAt;
    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
