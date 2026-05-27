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
class UploadProductImageUseCaseTest {

    @Mock private ProductGateway productGateway;

    @InjectMocks
    private UploadProductImageUseCase useCase;

    private final UUID productId = UUID.randomUUID();

    @Test
    void execute_returnsProductImage_whenProductFound() {
        Product p = Product.builder().id(productId).name("Café").price(BigDecimal.TEN)
                .status(ProductStatus.active).soldCount(0)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        when(productGateway.findById(productId)).thenReturn(Mono.just(p));

        StepVerifier.create(useCase.execute(productId, "http://img.url/photo.jpg", 1))
                .expectNextMatches(img -> "http://img.url/photo.jpg".equals(img.getImageUrl())
                        && productId.equals(img.getProductId()) && img.getDisplayOrder() == 1)
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenProductMissing() {
        when(productGateway.findById(productId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(productId, "http://img.url/photo.jpg", 1))
                .verifyError(NotFoundException.class);
    }
}
