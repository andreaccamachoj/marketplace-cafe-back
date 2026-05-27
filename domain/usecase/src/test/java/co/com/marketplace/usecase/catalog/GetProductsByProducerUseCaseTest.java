package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.ProductStatus;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.ProducerProfile;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProductsByProducerUseCaseTest {

    @Mock private ProductGateway productGateway;
    @Mock private ProducerProfileGateway producerProfileGateway;

    @InjectMocks
    private GetProductsByProducerUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID profileId = UUID.randomUUID();

    @Test
    void execute_returnsProducts_whenProfileFound() {
        ProducerProfile profile = ProducerProfile.builder().id(profileId).userId(userId).build();
        Product p = Product.builder().id(UUID.randomUUID()).producerId(profileId).name("Café")
                .price(BigDecimal.TEN).status(ProductStatus.active).soldCount(0)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();

        when(producerProfileGateway.findByUserId(userId)).thenReturn(Mono.just(profile));
        when(productGateway.findByProducerId(profileId, null, 0, 10)).thenReturn(Flux.just(p));

        StepVerifier.create(useCase.execute(userId, 0, 10))
                .expectNextMatches(prod -> profileId.equals(prod.getProducerId()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenProfileMissing() {
        when(producerProfileGateway.findByUserId(userId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(userId, 0, 10))
                .verifyError(NotFoundException.class);
    }
}
