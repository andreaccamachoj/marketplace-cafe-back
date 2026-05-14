package co.com.marketplace.usecase.reviews;

import co.com.marketplace.model.exception.ConflictException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateReviewUseCaseTest {

    @Mock private ReviewGateway reviewGateway;

    @InjectMocks
    private CreateReviewUseCase createReviewUseCase;

    private final UUID productId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final UUID buyerId = UUID.randomUUID();

    private CreateReviewUseCase.Command buildCommand() {
        return new CreateReviewUseCase.Command(productId, orderId, buyerId, (short) 5, "Great coffee", "Loved it");
    }

    private Review buildReview() {
        return Review.builder()
                .id(UUID.randomUUID()).productId(productId).buyerId(buyerId).orderId(orderId)
                .rating((short) 5).title("Great coffee").body("Loved it")
                .status(ReviewStatus.published).isVerifiedPurchase(true).helpfulCount(0)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void execute_savesReview_whenNoDuplicate() {
        Review savedReview = buildReview();
        when(reviewGateway.existsByBuyerAndProduct(buyerId, productId)).thenReturn(Mono.just(false));
        when(reviewGateway.save(any())).thenReturn(Mono.just(savedReview));

        StepVerifier.create(createReviewUseCase.execute(buildCommand()))
                .expectNextMatches(r -> r.getProductId().equals(productId) && r.getBuyerId().equals(buyerId))
                .verifyComplete();
    }

    @Test
    void execute_throwsConflict_whenReviewAlreadyExists() {
        when(reviewGateway.existsByBuyerAndProduct(buyerId, productId)).thenReturn(Mono.just(true));

        StepVerifier.create(createReviewUseCase.execute(buildCommand()))
                .verifyError(ConflictException.class);
    }
}
