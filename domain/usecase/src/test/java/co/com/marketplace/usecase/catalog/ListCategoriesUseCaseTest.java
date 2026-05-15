package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.Category;
import co.com.marketplace.model.catalog.gateways.CategoryGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListCategoriesUseCaseTest {

    @Mock private CategoryGateway categoryGateway;

    @InjectMocks
    private ListCategoriesUseCase useCase;

    @Test
    void execute_returnsCategories() {
        Category c = Category.builder().id(UUID.randomUUID()).name("Café").slug("cafe")
                .isActive(true).createdAt(OffsetDateTime.now()).build();
        when(categoryGateway.findAll()).thenReturn(Flux.just(c));

        StepVerifier.create(useCase.execute())
                .expectNextMatches(cat -> "cafe".equals(cat.getSlug()))
                .verifyComplete();
    }

    @Test
    void execute_returnsEmpty_whenNoCategories() {
        when(categoryGateway.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute())
                .verifyComplete();
    }
}
