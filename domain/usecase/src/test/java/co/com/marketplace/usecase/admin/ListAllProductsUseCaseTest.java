package co.com.marketplace.usecase.admin;

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
class ListAllProductsUseCaseTest {

    @Mock private ProductGateway productGateway;

    @InjectMocks
    private ListAllProductsUseCase useCase;

    @Test
    void execute_returnsAllProducts() {
        Product p = Product.builder().id(UUID.randomUUID()).name("Café").price(BigDecimal.TEN)
                .status(ProductStatus.active).soldCount(0)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        when(productGateway.findAllForAdmin(0, 20)).thenReturn(Flux.just(p));

        StepVerifier.create(useCase.execute(0, 20))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void execute_returnsEmpty_whenNoProducts() {
        when(productGateway.findAllForAdmin(0, 20)).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute(0, 20))
                .verifyComplete();
    }
}
