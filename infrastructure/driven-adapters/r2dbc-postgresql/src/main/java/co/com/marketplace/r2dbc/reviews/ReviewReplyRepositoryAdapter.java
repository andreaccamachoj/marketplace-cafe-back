package co.com.marketplace.r2dbc.reviews;

import co.com.marketplace.model.reviews.ReviewReply;
import co.com.marketplace.model.reviews.gateways.ReviewReplyGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewReplyRepositoryAdapter implements ReviewReplyGateway {

    private final ReviewReplyReactiveRepository repo;

    @Override
    public Mono<ReviewReply> save(ReviewReply reply) {
        return repo.save(toData(reply))
                .doOnSubscribe(s -> log.debug("[ReviewReplyRepositoryAdapter#save] DB request: reviewId={}", reply.getReviewId()))
                .doOnSuccess(r -> log.debug("[ReviewReplyRepositoryAdapter#save] DB response: result={}", r != null))
                .doOnError(e -> log.error("[ReviewReplyRepositoryAdapter#save] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    @Override
    public Mono<ReviewReply> findByReviewId(UUID reviewId) {
        return repo.findByReviewId(reviewId)
                .doOnSubscribe(s -> log.debug("[ReviewReplyRepositoryAdapter#findByReviewId] DB request: reviewId={}", reviewId))
                .doOnSuccess(r -> log.debug("[ReviewReplyRepositoryAdapter#findByReviewId] DB response: found={}", r != null))
                .doOnError(e -> log.error("[ReviewReplyRepositoryAdapter#findByReviewId] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    private ReviewReply toDomain(ReviewReplyData d) {
        return ReviewReply.builder()
                .id(d.getId())
                .reviewId(d.getReviewId())
                .producerId(d.getProducerId())
                .body(d.getBody())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    private ReviewReplyData toData(ReviewReply r) {
        return ReviewReplyData.builder()
                .id(r.getId())
                .reviewId(r.getReviewId())
                .producerId(r.getProducerId())
                .body(r.getBody())
                .createdAt(r.getCreatedAt())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}
