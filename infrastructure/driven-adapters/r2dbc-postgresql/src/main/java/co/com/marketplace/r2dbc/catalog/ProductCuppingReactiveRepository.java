package co.com.marketplace.r2dbc.catalog;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProductCuppingReactiveRepository extends ReactiveCrudRepository<ProductCuppingData, UUID> {
    Mono<ProductCuppingData> findByProductId(UUID productId);
}
