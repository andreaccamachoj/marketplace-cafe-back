package co.com.marketplace.r2dbc.catalog;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface ProductFlavorNoteReactiveRepository extends ReactiveCrudRepository<ProductFlavorNoteData, UUID> {
    Flux<ProductFlavorNoteData> findByProductId(UUID productId);
}
