package co.com.marketplace.r2dbc.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import co.com.marketplace.r2dbc.type.ProducerStatusType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(schema = "marketplace", name = "producer_approvals")
public class ProducerApprovalData {
    @Id
    private UUID id;
    @Column("producer_id")
    private UUID producerId;
    @Column("producer_name_snapshot")
    private String producerNameSnapshot;
    @Column("farm_name_snapshot")
    private String farmNameSnapshot;
    private String region;
    private String department;
    private BigDecimal hectares;
    @Column("main_variety")
    private String mainVariety;
    private String email;
    private String phone;
    private ProducerStatusType status;
    @Column("rejection_reason")
    private String rejectionReason;
    @Column("reviewed_by")
    private UUID reviewedBy;
    @Column("reviewed_at")
    private OffsetDateTime reviewedAt;
    @Column("submitted_at")
    private OffsetDateTime submittedAt;
}
