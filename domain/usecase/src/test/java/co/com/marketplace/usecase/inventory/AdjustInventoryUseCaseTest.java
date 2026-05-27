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
class AdjustInventoryUseCaseTest {

    @Mock private InventoryGateway inventoryGateway;

    @InjectMocks
    private AdjustInventoryUseCase useCase;

    private final UUID productId = UUID.randomUUID();

    @Test
    void execute_returnsAdjustedItem() {
        InventoryItem item = InventoryItem.builder().id(UUID.randomUUID()).productId(productId)
                .quantity(110).updatedAt(OffsetDateTime.now()).build();
        when(inventoryGateway.adjust(productId, 10)).thenReturn(Mono.just(item));

        StepVerifier.create(useCase.execute(productId, 10))
                .expectNextMatches(i -> i.getQuantity() == 110)
                .verifyComplete();
    }

    @Test
    void execute_propagatesError_whenGatewayFails() {
        when(inventoryGateway.adjust(productId, -5)).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(useCase.execute(productId, -5))
                .verifyError(RuntimeException.class);
    }
}
