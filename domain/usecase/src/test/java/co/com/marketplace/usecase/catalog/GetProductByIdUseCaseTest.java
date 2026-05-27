package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.ProductStatus;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import co.com.marketplace.model.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProductByIdUseCaseTest {

    @Mock private ProductGateway productGateway;

    @InjectMocks
    private GetProductByIdUseCase useCase;

    private final UUID productId = UUID.randomUUID();

    @Test
    void execute_returnsProduct_whenFound() {
        Product p = Product.builder().id(productId).name("Café").price(BigDecimal.TEN)
                .status(ProductStatus.active).soldCount(0)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        when(productGateway.findById(productId)).thenReturn(Mono.just(p));

        StepVerifier.create(useCase.execute(productId))
                .expectNextMatches(prod -> productId.equals(prod.getId()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenMissing() {
        when(productGateway.findById(productId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(productId))
                .verifyError(NotFoundException.class);
    }
}
