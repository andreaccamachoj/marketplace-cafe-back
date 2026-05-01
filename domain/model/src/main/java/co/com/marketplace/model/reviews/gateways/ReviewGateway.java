package co.com.marketplace.model.reviews.gateways;

import co.com.marketplace.model.reviews.Review;
import co.com.marketplace.model.reviews.ReviewStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ReviewGateway {
    Mono<Review> save(Review review);
    Mono<Review> findById(UUID id);
    Mono<Review> updateStatus(UUID id, ReviewStatus status);
    Flux<Review> findByProductId(UUID productId, int page, int size);
    Mono<Long> countByProductId(UUID productId);
    Flux<Review> findByProducerId(UUID producerId, int page, int size);
    Mono<Long> countByProducerId(UUID producerId);
    Mono<Boolean> existsByBuyerAndProduct(UUID buyerId, UUID productId);
}
