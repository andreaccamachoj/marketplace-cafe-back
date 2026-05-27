package co.com.marketplace.model.reviews;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
public class Review {
    UUID id;
    UUID productId;
    UUID buyerId;
    UUID orderId;
    short rating;
    String title;
    String body;
    ReviewStatus status;
    boolean isVerifiedPurchase;
    int helpfulCount;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
    String buyerName;
    String buyerInitials;
    String productName;
    String productEmoji;
    String producerReply;
    OffsetDateTime producerReplyDate;
}
