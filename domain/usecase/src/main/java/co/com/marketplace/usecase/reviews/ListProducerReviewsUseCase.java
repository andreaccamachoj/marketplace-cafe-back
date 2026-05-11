package co.com.marketplace.usecase.reviews;

import co.com.marketplace.model.reviews.Review;
import co.com.marketplace.model.reviews.gateways.ReviewGateway;
import reactor.core.publisher.Flux;

import java.util.UUID;

public final class ListProducerReviewsUseCase {

    private final ReviewGateway reviewGateway;

    public ListProducerReviewsUseCase(ReviewGateway reviewGateway) {
        this.reviewGateway = reviewGateway;
    }

    public Flux<Review> execute(UUID producerId, int page, int size) {
        return reviewGateway.findByProducerId(producerId, page, size);
    }
}
