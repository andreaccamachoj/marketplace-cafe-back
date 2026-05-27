package co.com.marketplace.model.admin;

import co.com.marketplace.model.identity.ProducerStatus;
import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class ProducerApproval {
    UUID id;
    UUID producerId;
    String producerNameSnapshot;
    String farmNameSnapshot;
    String region;
    String department;
    BigDecimal hectares;
    String mainVariety;
    String email;
    String phone;
    ProducerStatus status;
    String rejectionReason;
    UUID reviewedBy;
    OffsetDateTime reviewedAt;
    OffsetDateTime submittedAt;
}
