package co.com.marketplace.usecase.reviews;

import co.com.marketplace.model.reviews.Review;
import co.com.marketplace.model.reviews.ReviewStatus;
import co.com.marketplace.model.reviews.gateways.ReviewGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListProductReviewsUseCaseTest {

    @Mock private ReviewGateway reviewGateway;

    @InjectMocks
    private ListProductReviewsUseCase useCase;

    private final UUID productId = UUID.randomUUID();

    @Test
    void execute_returnsReviews() {
        Review review = Review.builder().id(UUID.randomUUID()).productId(productId)
                .buyerId(UUID.randomUUID()).rating((short) 5).status(ReviewStatus.published)
                .isVerifiedPurchase(true).helpfulCount(0)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        when(reviewGateway.findByProductId(productId, 0, 10)).thenReturn(Flux.just(review));

        StepVerifier.create(useCase.execute(productId, 0, 10))
                .expectNextMatches(r -> productId.equals(r.getProductId()))
                .verifyComplete();
    }

    @Test
    void execute_returnsEmpty_whenNoReviews() {
        when(reviewGateway.findByProductId(productId, 0, 10)).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute(productId, 0, 10))
                .verifyComplete();
    }
}
