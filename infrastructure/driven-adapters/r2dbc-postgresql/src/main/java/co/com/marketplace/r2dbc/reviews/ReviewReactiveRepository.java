package co.com.marketplace.r2dbc.reviews;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ReviewReactiveRepository extends ReactiveCrudRepository<ReviewData, UUID> {
    @Query("SELECT * FROM marketplace.reviews WHERE product_id = :productId ORDER BY created_at DESC LIMIT :size OFFSET :offset")
    Flux<ReviewData> findByProductId(UUID productId, int size, long offset);

    @Query("SELECT * FROM marketplace.reviews WHERE buyer_id = :buyerId ORDER BY created_at DESC LIMIT :size OFFSET :offset")
    Flux<ReviewData> findByBuyerId(UUID buyerId, int size, long offset);
}
