package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.gateways.ImageStorageGateway;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import co.com.marketplace.model.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteProductCoverImageUseCaseTest {

    @Mock
    private ProductGateway productGateway;
    @Mock
    private ImageStorageGateway imageStorageGateway;

    @InjectMocks
    private DeleteProductCoverImageUseCase useCase;

    private final UUID productId = UUID.randomUUID();

    @Test
    void execute_deletesFromStorageAndClearsUrl() {
        String key = "products/" + productId + "/cover";
        Product cleared = Product.builder().id(productId).build();

        when(productGateway.findById(productId)).thenReturn(Mono.just(Product.builder().id(productId).coverImageUrl("https://old").build()));
        when(imageStorageGateway.delete(key)).thenReturn(Mono.empty());
        when(productGateway.updateCoverImage(productId, null)).thenReturn(Mono.just(cleared));

        StepVerifier.create(useCase.execute(productId))
                .expectNextMatches(p -> p.getCoverImageUrl() == null)
                .verifyComplete();

        verify(imageStorageGateway).delete(key);
        verify(productGateway).updateCoverImage(productId, null);
    }

    @Test
    void execute_failsWhenProductNotFound() {
        when(productGateway.findById(productId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(productId))
                .verifyError(NotFoundException.class);
        verifyNoInteractions(imageStorageGateway);
    }
}
