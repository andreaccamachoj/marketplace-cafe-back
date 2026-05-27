package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.ProductStatus;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetFeaturedProductsUseCaseTest {

    @Mock private ProductGateway productGateway;

    @InjectMocks
    private GetFeaturedProductsUseCase useCase;

    @Test
    void execute_returnsFeaturedProducts() {
        Product p = Product.builder().id(UUID.randomUUID()).name("Café").price(BigDecimal.TEN)
                .status(ProductStatus.active).soldCount(5).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        when(productGateway.findFeatured(5)).thenReturn(Flux.just(p));

        StepVerifier.create(useCase.execute(5))
                .expectNextMatches(prod -> "Café".equals(prod.getName()))
                .verifyComplete();
    }

    @Test
    void execute_returnsEmpty_whenNoFeatured() {
        when(productGateway.findFeatured(5)).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute(5))
                .verifyComplete();
    }
}
