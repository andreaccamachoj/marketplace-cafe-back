package co.com.marketplace.api.reviews;

import co.com.marketplace.api.config.GlobalErrorWebExceptionHandler;
import co.com.marketplace.api.config.TestTxConfig;
import co.com.marketplace.model.reviews.Review;
import co.com.marketplace.model.reviews.ReviewStatus;
import co.com.marketplace.model.reviews.gateways.ReviewGateway;
import co.com.marketplace.usecase.reviews.CreateReviewUseCase;
import co.com.marketplace.usecase.reviews.ModerateReviewUseCase;
import co.com.marketplace.usecase.reviews.ReplyReviewUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorWebExceptionHandler.class, TestTxConfig.class})
class ReviewHandlerTest {

    private static final String USER_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Autowired private WebTestClient webTestClient;

    @MockitoBean private CreateReviewUseCase createReviewUseCase;
    @MockitoBean private ReplyReviewUseCase replyReviewUseCase;
    @MockitoBean private ModerateReviewUseCase moderateReviewUseCase;
    @MockitoBean private ReviewGateway reviewGateway;

    private Review buildReview() {
        return Review.builder()
                .id(UUID.randomUUID()).productId(UUID.randomUUID())
                .buyerId(UUID.fromString(USER_ID)).orderId(UUID.randomUUID())
                .rating((short) 5).title("Great").body("Good coffee")
                .status(ReviewStatus.published).isVerifiedPurchase(true)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void listMyReviews_returns200() {
        when(reviewGateway.findByBuyerId(any(), anyInt(), anyInt())).thenReturn(Flux.just(buildReview()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .get().uri("/api/reviews")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void moderate_returns200() {
        when(moderateReviewUseCase.execute(any(), any())).thenReturn(Mono.just(buildReview()));

        webTestClient.patch().uri("/api/reviews/" + UUID.randomUUID() + "/moderate")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"action":"published"}
                        """)
                .exchange()
                .expectStatus().isOk();
    }
}
