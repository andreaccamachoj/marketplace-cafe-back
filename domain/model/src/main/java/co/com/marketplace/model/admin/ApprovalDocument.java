package co.com.marketplace.model.admin;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
public class ApprovalDocument {
    UUID id;
    UUID approvalId;
    String name;
    String type;
    String url;
    OffsetDateTime uploadedAt;
}
