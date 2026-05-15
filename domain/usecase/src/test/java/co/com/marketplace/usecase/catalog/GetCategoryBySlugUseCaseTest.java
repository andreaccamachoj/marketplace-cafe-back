package co.com.marketplace.usecase.catalog;

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
class GetCategoryBySlugUseCaseTest {

    @Mock private CategoryGateway categoryGateway;

    @InjectMocks
    private GetCategoryBySlugUseCase useCase;

    @Test
    void execute_returnsCategory_whenFound() {
        Category c = Category.builder().id(UUID.randomUUID()).name("Café").slug("cafe")
                .isActive(true).createdAt(OffsetDateTime.now()).build();
        when(categoryGateway.findBySlug("cafe")).thenReturn(Mono.just(c));

        StepVerifier.create(useCase.execute("cafe"))
                .expectNextMatches(cat -> "cafe".equals(cat.getSlug()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenMissing() {
        when(categoryGateway.findBySlug("unknown")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute("unknown"))
                .verifyError(NotFoundException.class);
    }
}
