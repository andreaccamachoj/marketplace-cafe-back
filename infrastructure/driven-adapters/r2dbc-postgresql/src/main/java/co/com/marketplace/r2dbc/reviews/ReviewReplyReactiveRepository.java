package co.com.marketplace.r2dbc.reviews;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ReviewReplyReactiveRepository extends ReactiveCrudRepository<ReviewReplyData, UUID> {
    @Query("SELECT * FROM marketplace.review_replies WHERE review_id = :reviewId")
    Mono<ReviewReplyData> findByReviewId(UUID reviewId);
}
