package co.com.marketplace.usecase.admin;

import co.com.marketplace.model.catalog.Category;
import co.com.marketplace.model.catalog.gateways.CategoryGateway;
import co.com.marketplace.model.exception.ConflictException;
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
class CreateCategoryUseCaseTest {

    @Mock private CategoryGateway categoryGateway;

    @InjectMocks
    private CreateCategoryUseCase useCase;

    @Test
    void execute_createsCategory_whenSlugNotExists() {
        Category saved = Category.builder().id(UUID.randomUUID()).name("Café").slug("cafe")
                .isActive(true).createdAt(OffsetDateTime.now()).build();

        when(categoryGateway.findBySlug("cafe")).thenReturn(Mono.empty());
        when(categoryGateway.save(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(useCase.execute("Café", "cafe", "Desc", null, "☕"))
                .expectNextMatches(c -> "cafe".equals(c.getSlug()))
                .verifyComplete();
    }

    @Test
    void execute_throwsConflict_whenSlugExists() {
        Category existing = Category.builder().id(UUID.randomUUID()).name("Old Café").slug("cafe")
                .isActive(true).createdAt(OffsetDateTime.now()).build();

        when(categoryGateway.findBySlug("cafe")).thenReturn(Mono.just(existing));

        StepVerifier.create(useCase.execute("Café", "cafe", "Desc", null, "☕"))
                .verifyError(ConflictException.class);
    }
}
