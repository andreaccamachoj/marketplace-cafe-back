package co.com.marketplace.api.reviews;

import co.com.marketplace.api.config.GlobalErrorWebExceptionHandler;
import co.com.marketplace.api.config.TestTxConfig;
import co.com.marketplace.model.reviews.Review;
import co.com.marketplace.model.reviews.ReviewReply;
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
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorWebExceptionHandler.class, TestTxConfig.class})
class ReviewHandlerCreateTest {

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
                .rating((short) 5).title("Excelente").body("Muy buen café")
                .status(ReviewStatus.published).isVerifiedPurchase(true)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void create_returns201() {
        when(createReviewUseCase.execute(any())).thenReturn(Mono.just(buildReview()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .post().uri("/api/reviews")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"productId":"550e8400-e29b-41d4-a716-446655440002",
                         "orderId":"550e8400-e29b-41d4-a716-446655440003",
                         "rating":5,"title":"Excelente","body":"Muy buen café"}
                        """)
                .exchange()
                .expectStatus().is2xxSuccessful();
    }

    @Test
    void reply_returns201() {
        ReviewReply reply = ReviewReply.builder()
                .id(UUID.randomUUID()).reviewId(UUID.randomUUID())
                .producerId(UUID.fromString(USER_ID)).body("Gracias por tu reseña")
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();
        when(replyReviewUseCase.execute(any(), any(), any())).thenReturn(Mono.just(reply));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .post().uri("/api/reviews/" + UUID.randomUUID() + "/reply")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"body":"Gracias por tu reseña"}
                        """)
                .exchange()
                .expectStatus().is2xxSuccessful();
    }
}
