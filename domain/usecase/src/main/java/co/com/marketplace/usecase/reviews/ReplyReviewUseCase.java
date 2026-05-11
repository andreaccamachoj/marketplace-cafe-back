package co.com.marketplace.usecase.reviews;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.reviews.ReviewReply;
import co.com.marketplace.model.reviews.gateways.ReviewGateway;
import co.com.marketplace.model.reviews.gateways.ReviewReplyGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public final class ReplyReviewUseCase {

    private final ReviewGateway reviewGateway;
    private final ReviewReplyGateway reviewReplyGateway;

    public Mono<ReviewReply> execute(UUID reviewId, UUID producerId, String body) {
        return reviewGateway.findById(reviewId)
                .switchIfEmpty(Mono.error(new NotFoundException("REVIEW_NOT_FOUND", "Review not found: " + reviewId)))
                .flatMap(review -> {
                    ReviewReply reply = ReviewReply.builder()
                            .id(UUID.randomUUID())
                            .reviewId(reviewId)
                            .producerId(producerId)
                            .body(body)
                            .createdAt(OffsetDateTime.now())
                            .updatedAt(OffsetDateTime.now())
                            .build();
                    return reviewReplyGateway.save(reply);
                });
    }
}
