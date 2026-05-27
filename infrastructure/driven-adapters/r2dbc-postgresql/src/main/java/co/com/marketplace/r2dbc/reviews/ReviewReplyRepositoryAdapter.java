package co.com.marketplace.r2dbc.reviews;

import co.com.marketplace.model.reviews.ReviewReply;
import co.com.marketplace.model.reviews.gateways.ReviewReplyGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewReplyRepositoryAdapter implements ReviewReplyGateway {

    private final ReviewReplyReactiveRepository repo;
    private final DatabaseClient db;

    @Override
    public Mono<ReviewReply> save(ReviewReply reply) {
        UUID id = reply.getId() != null ? reply.getId() : UUID.randomUUID();
        return db.sql(
                "INSERT INTO marketplace.review_replies (id, review_id, producer_id, body, created_at, updated_at) " +
                "VALUES (:id, :reviewId, :producerId, :body, NOW(), NOW()) " +
                "ON CONFLICT (review_id) DO UPDATE SET body = EXCLUDED.body, updated_at = NOW() " +
                "RETURNING *")
                .bind("id", id)
                .bind("reviewId", reply.getReviewId())
                .bind("producerId", reply.getProducerId())
                .bind("body", reply.getBody())
                .map((row, meta) -> ReviewReply.builder()
                        .id(row.get("id", UUID.class))
                        .reviewId(row.get("review_id", UUID.class))
                        .producerId(row.get("producer_id", UUID.class))
                        .body(row.get("body", String.class))
                        .createdAt(row.get("created_at", OffsetDateTime.class))
                        .updatedAt(row.get("updated_at", OffsetDateTime.class))
                        .build())
                .one()
                .doOnSubscribe(s -> log.debug("[ReviewReplyRepositoryAdapter#save] DB request: reviewId={}", reply.getReviewId()))
                .doOnSuccess(r -> log.debug("[ReviewReplyRepositoryAdapter#save] DB response: id={}", r != null ? r.getId() : null))
                .doOnError(e -> log.error("[ReviewReplyRepositoryAdapter#save] DB error: {}", e.getMessage()));
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
