package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.gateways.ImageStorageGateway;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateProductCoverImageUseCaseTest {

    @Mock
    private ProductGateway productGateway;
    @Mock
    private ImageStorageGateway imageStorageGateway;

    @InjectMocks
    private UpdateProductCoverImageUseCase useCase;

    private final UUID productId = UUID.randomUUID();

    @Test
    void execute_uploadsAndPersistsUrl_whenValidPng() {
        byte[] content = {1, 2, 3};
        String expectedKey = "products/" + productId + "/cover";
        String url = "https://bucket.s3.amazonaws.com/" + expectedKey;
        Product persisted = Product.builder().id(productId).coverImageUrl(url).build();

        when(productGateway.findById(productId)).thenReturn(Mono.just(Product.builder().id(productId).build()));
        when(imageStorageGateway.upload(content, "image/png", expectedKey)).thenReturn(Mono.just(url));
        when(productGateway.updateCoverImage(productId, url)).thenReturn(Mono.just(persisted));

        StepVerifier.create(useCase.execute(productId, content, "image/png"))
                .expectNextMatches(p -> url.equals(p.getCoverImageUrl()))
                .verifyComplete();

        verify(imageStorageGateway).upload(content, "image/png", expectedKey);
    }

    @Test
    void execute_acceptsJpeg() {
        byte[] content = {9};
        String key = "products/" + productId + "/cover";
        String url = "https://bucket/" + key;

        when(productGateway.findById(productId)).thenReturn(Mono.just(Product.builder().id(productId).build()));
        when(imageStorageGateway.upload(any(), eq("image/jpeg"), eq(key))).thenReturn(Mono.just(url));
        when(productGateway.updateCoverImage(productId, url)).thenReturn(Mono.just(Product.builder().id(productId).coverImageUrl(url).build()));

        StepVerifier.create(useCase.execute(productId, content, "image/jpeg"))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void execute_failsWithInvalidFormat() {
        StepVerifier.create(useCase.execute(productId, new byte[]{1}, "application/pdf"))
                .verifyError(ValidationException.class);
        verifyNoInteractions(productGateway, imageStorageGateway);
    }

    @Test
    void execute_failsWhenTooLarge() {
        byte[] tooBig = new byte[(2 * 1024 * 1024) + 1];
        StepVerifier.create(useCase.execute(productId, tooBig, "image/png"))
                .verifyError(ValidationException.class);
        verifyNoInteractions(productGateway, imageStorageGateway);
    }

    @Test
    void execute_failsWhenEmpty() {
        StepVerifier.create(useCase.execute(productId, new byte[0], "image/png"))
                .verifyError(ValidationException.class);
        verifyNoInteractions(productGateway, imageStorageGateway);
    }

    @Test
    void execute_failsWhenProductNotFound() {
        when(productGateway.findById(productId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(productId, new byte[]{1}, "image/png"))
                .verifyError(NotFoundException.class);
        verifyNoInteractions(imageStorageGateway);
    }
}
