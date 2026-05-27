package co.com.marketplace.model.catalog.gateways;

import co.com.marketplace.model.catalog.Category;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CategoryGateway {
    Flux<Category> findAll();
    Mono<Category> findById(UUID id);
    Mono<Category> findBySlug(String slug);
    Mono<Category> save(Category category);
    Mono<Category> update(Category category);
    Mono<Void> deleteById(UUID id);
}
