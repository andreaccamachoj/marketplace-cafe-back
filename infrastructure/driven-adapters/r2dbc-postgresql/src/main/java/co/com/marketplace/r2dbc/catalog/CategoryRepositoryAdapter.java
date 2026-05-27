package co.com.marketplace.r2dbc.catalog;

import co.com.marketplace.model.catalog.Category;
import co.com.marketplace.model.catalog.gateways.CategoryGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.data.relational.core.query.Update;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryGateway {

    private final CategoryReactiveRepository repository;
    private final R2dbcEntityTemplate template;

    @Override
    public Flux<Category> findAll() {
        return repository.findAll()
                .doOnSubscribe(s -> log.debug("[CategoryRepositoryAdapter#findAll] DB request"))
                .doOnComplete(() -> log.debug("[CategoryRepositoryAdapter#findAll] DB response: complete"))
                .doOnError(e -> log.error("[CategoryRepositoryAdapter#findAll] DB error: {}", e.getMessage()))
                .map(CategoryRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<Category> findById(UUID id) {
        return repository.findById(id)
                .doOnSubscribe(s -> log.debug("[CategoryRepositoryAdapter#findById] DB request: id={}", id))
                .doOnSuccess(r -> log.debug("[CategoryRepositoryAdapter#findById] DB response: found={}", r != null))
                .doOnError(e -> log.error("[CategoryRepositoryAdapter#findById] DB error: {}", e.getMessage()))
                .map(CategoryRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<Category> findBySlug(String slug) {
        return repository.findBySlug(slug)
                .doOnSubscribe(s -> log.debug("[CategoryRepositoryAdapter#findBySlug] DB request: slug={}", slug))
                .doOnSuccess(r -> log.debug("[CategoryRepositoryAdapter#findBySlug] DB response: found={}", r != null))
                .doOnError(e -> log.error("[CategoryRepositoryAdapter#findBySlug] DB error: {}", e.getMessage()))
                .map(CategoryRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<Category> save(Category category) {
        return repository.save(toData(category))
                .doOnSubscribe(s -> log.debug("[CategoryRepositoryAdapter#save] DB request: name={}", category.getName()))
                .doOnSuccess(r -> log.debug("[CategoryRepositoryAdapter#save] DB response: result={}", r != null))
                .doOnError(e -> log.error("[CategoryRepositoryAdapter#save] DB error: {}", e.getMessage()))
                .map(CategoryRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<Category> update(Category category) {
        return template.update(
                Query.query(Criteria.where("id").is(category.getId())),
                Update.update("name", category.getName())
                        .set("slug", category.getSlug())
                        .set("description", category.getDescription())
                        .set("parent_id", category.getParentId())
                        .set("is_active", category.isActive())
                        .set("icon_emoji", category.getIconEmoji()),
                CategoryData.class
        ).doOnSubscribe(s -> log.debug("[CategoryRepositoryAdapter#update] DB request: id={}", category.getId()))
                .doOnSuccess(r -> log.debug("[CategoryRepositoryAdapter#update] DB response: result={}", r))
                .doOnError(e -> log.error("[CategoryRepositoryAdapter#update] DB error: {}", e.getMessage()))
                .then(repository.findById(category.getId()).map(CategoryRepositoryAdapter::toDomain));
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return repository.deleteById(id)
                .doOnSubscribe(s -> log.debug("[CategoryRepositoryAdapter#deleteById] DB request: id={}", id))
                .doOnTerminate(() -> log.debug("[CategoryRepositoryAdapter#deleteById] DB response: done"))
                .doOnError(e -> log.error("[CategoryRepositoryAdapter#deleteById] DB error: {}", e.getMessage()));
    }

    static Category toDomain(CategoryData d) {
        return Category.builder()
                .id(d.getId())
                .name(d.getName())
                .slug(d.getSlug())
                .description(d.getDescription())
                .parentId(d.getParentId())
                .isActive(d.isActive())
                .iconEmoji(d.getIconEmoji())
                .createdAt(d.getCreatedAt())
                .build();
    }

    static CategoryData toData(Category c) {
        return CategoryData.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .description(c.getDescription())
                .parentId(c.getParentId())
                .isActive(c.isActive())
                .iconEmoji(c.getIconEmoji())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
