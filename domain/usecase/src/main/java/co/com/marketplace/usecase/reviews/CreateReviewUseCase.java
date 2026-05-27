package co.com.marketplace.usecase.reviews;

import co.com.marketplace.model.exception.ConflictException;
import co.com.marketplace.model.reviews.Review;
import co.com.marketplace.model.reviews.ReviewStatus;
import co.com.marketplace.model.reviews.gateways.ReviewGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public final class CreateReviewUseCase {

    private final ReviewGateway reviewGateway;

    public record Command(
            UUID productId,
            UUID orderId,
            UUID buyerId,
            short rating,
            String title,
            String body
    ) {}

    public Mono<Review> execute(Command cmd) {
        return reviewGateway.existsByBuyerAndProduct(cmd.buyerId(), cmd.productId())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ConflictException("REVIEW_ALREADY_EXISTS", "Buyer already reviewed this product"));
                    }
                    Review review = Review.builder()
                            .id(UUID.randomUUID())
                            .productId(cmd.productId())
                            .orderId(cmd.orderId())
                            .buyerId(cmd.buyerId())
                            .rating(cmd.rating())
                            .title(cmd.title())
                            .body(cmd.body())
                            .status(ReviewStatus.published)
                            .isVerifiedPurchase(cmd.orderId() != null)
                            .helpfulCount(0)
                            .createdAt(OffsetDateTime.now())
                            .updatedAt(OffsetDateTime.now())
                            .build();
                    return reviewGateway.save(review);
                });
    }
}
