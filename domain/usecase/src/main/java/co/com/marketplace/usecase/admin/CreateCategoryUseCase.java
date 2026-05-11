package co.com.marketplace.usecase.admin;

import co.com.marketplace.model.catalog.Category;
import co.com.marketplace.model.catalog.gateways.CategoryGateway;
import co.com.marketplace.model.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public final class CreateCategoryUseCase {

    private final CategoryGateway categoryGateway;

    public Mono<Category> execute(String name, String slug, String description,
                                   UUID parentId, String iconEmoji) {
        return categoryGateway.findBySlug(slug)
                .flatMap(existing -> Mono.<Category>error(new ConflictException("CATEGORY_SLUG_EXISTS", "Category slug already exists: " + slug)))
                .switchIfEmpty(Mono.defer(() -> {
                    Category category = Category.builder()
                            .id(UUID.randomUUID())
                            .name(name)
                            .slug(slug)
                            .description(description)
                            .parentId(parentId)
                            .isActive(true)
                            .iconEmoji(iconEmoji)
                            .createdAt(OffsetDateTime.now())
                            .build();
                    return categoryGateway.save(category);
                }));
    }
}
