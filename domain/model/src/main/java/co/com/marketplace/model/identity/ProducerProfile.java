package co.com.marketplace.model.identity;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class ProducerProfile {
    UUID id;
    UUID userId;
    String bio;
    String city;
    String department;
    ProducerStatus status;
    String rejectionReason;
    UUID approvedBy;
    OffsetDateTime approvedAt;
    String avatarInitials;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
