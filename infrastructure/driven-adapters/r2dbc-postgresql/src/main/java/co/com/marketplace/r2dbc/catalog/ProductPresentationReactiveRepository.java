package co.com.marketplace.r2dbc.catalog;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ProductPresentationReactiveRepository extends ReactiveCrudRepository<ProductPresentationData, UUID> {
    Flux<ProductPresentationData> findByProductId(UUID productId);
}
