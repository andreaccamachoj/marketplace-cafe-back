package co.com.marketplace.model.admin;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder
public class AdminActivityLog {
    UUID id;
    UUID actorId;
    String actorNameSnapshot;
    String type;
    String title;
    String description;
    String severity;
    String iconEmoji;
    String metadata;
    OffsetDateTime createdAt;
}
