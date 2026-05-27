package co.com.marketplace.usecase.admin;

import co.com.marketplace.model.catalog.Category;
import co.com.marketplace.model.catalog.gateways.CategoryGateway;
import co.com.marketplace.model.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateCategoryUseCaseTest {

    @Mock private CategoryGateway categoryGateway;

    @InjectMocks
    private UpdateCategoryUseCase useCase;

    private final UUID categoryId = UUID.randomUUID();

    @Test
    void execute_updatesCategory_whenFound() {
        Category existing = Category.builder().id(categoryId).name("Old").slug("old")
                .isActive(true).createdAt(OffsetDateTime.now()).build();
        Category updated = existing.toBuilder().name("New").slug("new").build();

        when(categoryGateway.findById(categoryId)).thenReturn(Mono.just(existing));
        when(categoryGateway.update(any())).thenReturn(Mono.just(updated));

        StepVerifier.create(useCase.execute(categoryId, "New", "new", "Desc", null, "☕", true))
                .expectNextMatches(c -> "New".equals(c.getName()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenCategoryMissing() {
        when(categoryGateway.findById(categoryId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(categoryId, "New", "new", "Desc", null, "☕", true))
                .verifyError(NotFoundException.class);
    }
}
