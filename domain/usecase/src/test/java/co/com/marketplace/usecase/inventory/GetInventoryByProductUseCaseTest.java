package co.com.marketplace.usecase.inventory;

import co.com.marketplace.model.inventory.InventoryItem;
import co.com.marketplace.model.inventory.gateways.InventoryGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetInventoryByProductUseCaseTest {

    @Mock private InventoryGateway inventoryGateway;

    @InjectMocks
    private GetInventoryByProductUseCase useCase;

    private final UUID productId = UUID.randomUUID();

    @Test
    void execute_returnsInventoryItem_whenFound() {
        InventoryItem item = InventoryItem.builder().id(UUID.randomUUID()).productId(productId)
                .quantity(50).updatedAt(OffsetDateTime.now()).build();
        when(inventoryGateway.findByProductId(productId)).thenReturn(Mono.just(item));

        StepVerifier.create(useCase.execute(productId))
                .expectNextMatches(i -> i.getQuantity() == 50)
                .verifyComplete();
    }

    @Test
    void execute_returnsEmpty_whenNotFound() {
        when(inventoryGateway.findByProductId(productId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(productId))
                .verifyComplete();
    }
}
