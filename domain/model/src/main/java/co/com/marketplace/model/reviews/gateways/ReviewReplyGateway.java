package co.com.marketplace.model.reviews.gateways;

import co.com.marketplace.model.reviews.ReviewReply;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ReviewReplyGateway {
    Mono<ReviewReply> save(ReviewReply reply);
    Mono<ReviewReply> findByReviewId(UUID reviewId);
}
