package co.com.marketplace.r2dbc.catalog;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProductReactiveRepository extends ReactiveCrudRepository<ProductData, UUID> {

    @Query("SELECT * FROM marketplace.products WHERE producer_id = :producerId AND (:status IS NULL OR status = :status) ORDER BY created_at DESC LIMIT :size OFFSET :offset")
    Flux<ProductData> findByProducerIdAndStatus(UUID producerId, String status, int size, long offset);

    @Query("SELECT COUNT(*) FROM marketplace.products WHERE producer_id = :producerId AND (:status IS NULL OR status = :status)")
    Mono<Long> countByProducerIdAndStatus(UUID producerId, String status);

    @Query("SELECT * FROM marketplace.products WHERE status = 'active' ORDER BY sold_count DESC LIMIT :limit")
    Flux<ProductData> findFeatured(int limit);
}
