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

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteCategoryUseCaseTest {

    @Mock private CategoryGateway categoryGateway;

    @InjectMocks
    private DeleteCategoryUseCase useCase;

    private final UUID categoryId = UUID.randomUUID();

    @Test
    void execute_deletesSuccessfully_whenFound() {
        Category category = Category.builder().id(categoryId).name("Café").slug("cafe")
                .isActive(true).createdAt(OffsetDateTime.now()).build();

        when(categoryGateway.findById(categoryId)).thenReturn(Mono.just(category));
        when(categoryGateway.deleteById(categoryId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(categoryId))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenCategoryMissing() {
        when(categoryGateway.findById(categoryId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(categoryId))
                .verifyError(NotFoundException.class);
    }
}
