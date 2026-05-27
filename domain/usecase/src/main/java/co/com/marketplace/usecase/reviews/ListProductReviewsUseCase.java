package co.com.marketplace.usecase.reviews;

import co.com.marketplace.model.reviews.Review;
import co.com.marketplace.model.reviews.gateways.ReviewGateway;
import reactor.core.publisher.Flux;

import java.util.UUID;

public final class ListProductReviewsUseCase {

    private final ReviewGateway reviewGateway;

    public ListProductReviewsUseCase(ReviewGateway reviewGateway) {
        this.reviewGateway = reviewGateway;
    }

    public Flux<Review> execute(UUID productId, int page, int size) {
        return reviewGateway.findByProductId(productId, page, size);
    }
}
