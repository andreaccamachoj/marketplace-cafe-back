package co.com.marketplace.r2dbc.catalog;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ProductImageReactiveRepository extends ReactiveCrudRepository<ProductImageData, UUID> {
    Flux<ProductImageData> findByProductId(UUID productId);
}
