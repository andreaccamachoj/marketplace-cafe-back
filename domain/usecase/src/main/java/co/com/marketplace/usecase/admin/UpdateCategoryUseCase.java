package co.com.marketplace.usecase.admin;

import co.com.marketplace.model.catalog.Category;
import co.com.marketplace.model.catalog.gateways.CategoryGateway;
import co.com.marketplace.model.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public final class UpdateCategoryUseCase {

    private final CategoryGateway categoryGateway;

    public Mono<Category> execute(UUID id, String name, String slug, String description,
                                   UUID parentId, String iconEmoji, boolean isActive) {
        return categoryGateway.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("CATEGORY_NOT_FOUND", "Category not found: " + id)))
                .flatMap(existing -> {
                    Category updated = existing.toBuilder()
                            .name(name)
                            .slug(slug)
                            .description(description)
                            .parentId(parentId)
                            .iconEmoji(iconEmoji)
                            .isActive(isActive)
                            .build();
                    return categoryGateway.update(updated);
                });
    }
}
