package co.com.marketplace.usecase.admin;

import co.com.marketplace.model.catalog.gateways.CategoryGateway;
import co.com.marketplace.model.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public final class DeleteCategoryUseCase {

    private final CategoryGateway categoryGateway;

    public Mono<Void> execute(UUID id) {
        return categoryGateway.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("CATEGORY_NOT_FOUND", "Category not found: " + id)))
                .flatMap(existing -> categoryGateway.deleteById(id));
    }
}
