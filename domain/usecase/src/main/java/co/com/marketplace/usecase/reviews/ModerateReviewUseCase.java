package co.com.marketplace.usecase.reviews;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.reviews.Review;
import co.com.marketplace.model.reviews.ReviewStatus;
import co.com.marketplace.model.reviews.gateways.ReviewGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public final class ModerateReviewUseCase {

    private final ReviewGateway reviewGateway;

    public Mono<Review> execute(UUID reviewId, ReviewStatus newStatus) {
        return reviewGateway.findById(reviewId)
                .switchIfEmpty(Mono.error(new NotFoundException("REVIEW_NOT_FOUND", "Review not found: " + reviewId)))
                .flatMap(review -> reviewGateway.updateStatus(reviewId, newStatus));
    }
}
