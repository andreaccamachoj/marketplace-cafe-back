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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListProductsUseCaseTest {

    @Mock private ProductGateway productGateway;

    @InjectMocks
    private ListProductsUseCase listProductsUseCase;

    @Test
    void execute_returnsProducts_fromGateway() {
        Product product = Product.builder()
                .id(UUID.randomUUID()).name("Ethiopian Coffee").price(BigDecimal.valueOf(25.99))
                .status(ProductStatus.active).createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now())
                .build();

        when(productGateway.findAll(isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), anyInt(), anyInt(), anyString()))
                .thenReturn(Flux.just(product));

        ListProductsUseCase.Command cmd = new ListProductsUseCase.Command(null, null, null, null, null, null, null, 0, 20, "createdAt,desc");

        StepVerifier.create(listProductsUseCase.execute(cmd))
                .expectNextMatches(p -> "Ethiopian Coffee".equals(p.getName()))
                .verifyComplete();
    }
}
