package co.com.marketplace.r2dbc.catalog;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CategoryReactiveRepository extends ReactiveCrudRepository<CategoryData, UUID> {
    Mono<CategoryData> findBySlug(String slug);
}
