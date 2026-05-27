package co.com.marketplace.usecase.reviews;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.reviews.Review;
import co.com.marketplace.model.reviews.ReviewStatus;
import co.com.marketplace.model.reviews.gateways.ReviewGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ModerateReviewUseCaseTest {

    @Mock private ReviewGateway reviewGateway;

    @InjectMocks
    private ModerateReviewUseCase useCase;

    private final UUID reviewId = UUID.randomUUID();

    @Test
    void execute_moderatesReview_whenFound() {
        Review review = Review.builder().id(reviewId).productId(UUID.randomUUID())
                .buyerId(UUID.randomUUID()).rating((short) 1).status(ReviewStatus.published)
                .isVerifiedPurchase(false).helpfulCount(0)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        Review hidden = review.toBuilder().status(ReviewStatus.hidden).build();

        when(reviewGateway.findById(reviewId)).thenReturn(Mono.just(review));
        when(reviewGateway.updateStatus(reviewId, ReviewStatus.hidden)).thenReturn(Mono.just(hidden));

        StepVerifier.create(useCase.execute(reviewId, ReviewStatus.hidden))
                .expectNextMatches(r -> ReviewStatus.hidden.equals(r.getStatus()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenReviewMissing() {
        when(reviewGateway.findById(reviewId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(reviewId, ReviewStatus.hidden))
                .verifyError(NotFoundException.class);
    }
}
