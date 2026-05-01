package co.com.marketplace.model.notifications;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class Notification {
    UUID id;
    UUID userId;
    String type;
    String message;
    boolean isRead;
    String metadata;
    OffsetDateTime createdAt;
}
