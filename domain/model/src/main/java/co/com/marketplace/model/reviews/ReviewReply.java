package co.com.marketplace.model.reviews;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class ReviewReply {
    UUID id;
    UUID reviewId;
    UUID producerId;
    String body;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
