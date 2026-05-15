package co.com.marketplace.usecase.reviews;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.reviews.Review;
import co.com.marketplace.model.reviews.ReviewReply;
import co.com.marketplace.model.reviews.ReviewStatus;
import co.com.marketplace.model.reviews.gateways.ReviewGateway;
import co.com.marketplace.model.reviews.gateways.ReviewReplyGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReplyReviewUseCaseTest {

    @Mock private ReviewGateway reviewGateway;
    @Mock private ReviewReplyGateway reviewReplyGateway;

    @InjectMocks
    private ReplyReviewUseCase useCase;

    private final UUID reviewId = UUID.randomUUID();
    private final UUID producerId = UUID.randomUUID();

    @Test
    void execute_savesReply_whenReviewFound() {
        Review review = Review.builder().id(reviewId).productId(UUID.randomUUID())
                .buyerId(UUID.randomUUID()).rating((short) 4).status(ReviewStatus.published)
                .isVerifiedPurchase(true).helpfulCount(0)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        ReviewReply saved = ReviewReply.builder().id(UUID.randomUUID()).reviewId(reviewId)
                .producerId(producerId).body("Thank you!").createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now()).build();

        when(reviewGateway.findById(reviewId)).thenReturn(Mono.just(review));
        when(reviewReplyGateway.save(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(useCase.execute(reviewId, producerId, "Thank you!"))
                .expectNextMatches(r -> "Thank you!".equals(r.getBody()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenReviewMissing() {
        when(reviewGateway.findById(reviewId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(reviewId, producerId, "Thanks"))
                .verifyError(NotFoundException.class);
    }
}
