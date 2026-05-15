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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SearchProductsUseCaseTest {

    @Mock private ProductGateway productGateway;

    @InjectMocks
    private SearchProductsUseCase useCase;

    @Test
    void execute_returnsMatchingProducts() {
        Product p = Product.builder().id(UUID.randomUUID()).name("Café Especial")
                .price(BigDecimal.TEN).status(ProductStatus.active).soldCount(0)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        when(productGateway.findAll(eq("café"), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(10), isNull()))
                .thenReturn(Flux.just(p));

        StepVerifier.create(useCase.execute("café", 0, 10))
                .expectNextMatches(prod -> "Café Especial".equals(prod.getName()))
                .verifyComplete();
    }

    @Test
    void execute_returnsEmpty_whenNoMatches() {
        when(productGateway.findAll(eq("xyz"), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), eq(0), eq(10), isNull()))
                .thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute("xyz", 0, 10))
                .verifyComplete();
    }
}
