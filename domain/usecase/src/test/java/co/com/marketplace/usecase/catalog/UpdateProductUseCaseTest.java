package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.ProductStatus;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import co.com.marketplace.model.exception.ForbiddenException;
import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.ProducerProfile;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateProductUseCaseTest {

    @Mock private ProductGateway productGateway;
    @Mock private ProducerProfileGateway producerProfileGateway;

    @InjectMocks
    private UpdateProductUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID profileId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    private UpdateProductUseCase.Command cmd() {
        return new UpdateProductUseCase.Command("Café Updated", "Desc", BigDecimal.TEN, "kg", "Antioquia", "☕", null, 50, null, List.of());
    }

    @Test
    void execute_updatesProduct_whenOwnerMatches() {
        ProducerProfile profile = ProducerProfile.builder().id(profileId).userId(userId).build();
        Product existing = Product.builder().id(productId).producerId(profileId).name("Café")
                .price(BigDecimal.TEN).status(ProductStatus.active).soldCount(0)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        Product updated = existing.toBuilder().name("Café Updated").build();

        when(producerProfileGateway.findByUserId(userId)).thenReturn(Mono.just(profile));
        when(productGateway.findById(productId)).thenReturn(Mono.just(existing));
        when(productGateway.update(any())).thenReturn(Mono.just(updated));

        StepVerifier.create(useCase.execute(productId, userId, cmd()))
                .expectNextMatches(p -> "Café Updated".equals(p.getName()))
                .verifyComplete();
    }

    @Test
    void execute_throwsForbidden_whenOwnerMismatch() {
        UUID otherProfileId = UUID.randomUUID();
        ProducerProfile profile = ProducerProfile.builder().id(profileId).userId(userId).build();
        Product existing = Product.builder().id(productId).producerId(otherProfileId).name("Café")
                .price(BigDecimal.TEN).status(ProductStatus.active).soldCount(0)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();

        when(producerProfileGateway.findByUserId(userId)).thenReturn(Mono.just(profile));
        when(productGateway.findById(productId)).thenReturn(Mono.just(existing));

        StepVerifier.create(useCase.execute(productId, userId, cmd()))
                .verifyError(ForbiddenException.class);
    }

    @Test
    void execute_throwsNotFound_whenProfileMissing() {
        when(producerProfileGateway.findByUserId(userId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(productId, userId, cmd()))
                .verifyError(NotFoundException.class);
    }
}
