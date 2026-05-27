package co.com.marketplace.model.audit;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
public class AuditLog {
    UUID id;
    UUID userId;
    String action;
    String entityType;
    String entityId;
    String ipAddress;
    String metadata;
    OffsetDateTime createdAt;
}
