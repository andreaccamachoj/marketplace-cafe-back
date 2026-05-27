package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.ProductStatus;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.ProducerProfile;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
import co.com.marketplace.model.inventory.InventoryItem;
import co.com.marketplace.model.inventory.gateways.InventoryGateway;
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
class CreateProductUseCaseTest {

    @Mock private ProductGateway productGateway;
    @Mock private InventoryGateway inventoryGateway;
    @Mock private ProducerProfileGateway producerProfileGateway;

    @InjectMocks
    private CreateProductUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID profileId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();

    @Test
    void execute_createsProductAndInventory_whenProfileFound() {
        ProducerProfile profile = ProducerProfile.builder().id(profileId).userId(userId).build();
        Product saved = Product.builder().id(UUID.randomUUID()).producerId(profileId).name("Café")
                .price(BigDecimal.TEN).status(ProductStatus.draft).soldCount(0)
                .createdAt(OffsetDateTime.now()).updatedAt(OffsetDateTime.now()).build();
        InventoryItem inv = InventoryItem.builder().id(UUID.randomUUID()).productId(saved.getId())
                .quantity(100).updatedAt(OffsetDateTime.now()).build();

        when(producerProfileGateway.findByUserId(userId)).thenReturn(Mono.just(profile));
        when(productGateway.save(any())).thenReturn(Mono.just(saved));
        when(inventoryGateway.save(any())).thenReturn(Mono.just(inv));

        CreateProductUseCase.Command cmd = new CreateProductUseCase.Command(
                userId, categoryId, "Café", "Desc", BigDecimal.TEN, "kg", "Antioquia", "☕", 100, null, List.of());

        StepVerifier.create(useCase.execute(cmd))
                .expectNextMatches(p -> profileId.equals(p.getProducerId()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenProfileMissing() {
        when(producerProfileGateway.findByUserId(userId)).thenReturn(Mono.empty());

        CreateProductUseCase.Command cmd = new CreateProductUseCase.Command(
                userId, categoryId, "Café", "Desc", BigDecimal.TEN, "kg", "Antioquia", "☕", 100, null, List.of());

        StepVerifier.create(useCase.execute(cmd))
                .verifyError(NotFoundException.class);
    }
}
