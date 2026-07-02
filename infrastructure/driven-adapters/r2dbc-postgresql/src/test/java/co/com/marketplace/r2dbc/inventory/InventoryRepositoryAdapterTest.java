package co.com.marketplace.r2dbc.inventory;

import co.com.marketplace.model.inventory.InventoryItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.BiFunction;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
class InventoryRepositoryAdapterTest {

    @Mock private InventoryReactiveRepository repository;
    @Mock private DatabaseClient databaseClient;
    @Mock private DatabaseClient.GenericExecuteSpec spec;
    @Mock private RowsFetchSpec fetchSpec;

    @InjectMocks private InventoryRepositoryAdapter adapter;

    private final UUID itemId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID producerId = UUID.randomUUID();
    private InventoryData inventoryData;
    private InventoryItem inventoryItem;

    @BeforeEach
    void setUp() {
        inventoryData = InventoryData.builder()
                .id(itemId)
                .productId(productId)
                .quantity(100)
                .maxStock(500)
                .updatedAt(OffsetDateTime.now())
                .build();

        inventoryItem = InventoryItem.builder()
                .id(itemId)
                .productId(productId)
                .quantity(100)
                .maxStock(500)
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void save_returnsItem_whenSuccessful() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(inventoryData)).when(fetchSpec).one();

        StepVerifier.create(adapter.save(inventoryItem))
                .expectNextMatches(i -> itemId.equals(i.getId()) && 100 == i.getQuantity())
                .verifyComplete();
    }

    @Test
    void save_propagatesError_whenDatabaseFails() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.error(new RuntimeException("DB error"))).when(fetchSpec).one();

        StepVerifier.create(adapter.save(inventoryItem))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findByProductId_returnsItem_whenFound() {
        when(repository.findByProductId(productId)).thenReturn(Mono.just(inventoryData));

        StepVerifier.create(adapter.findByProductId(productId))
                .expectNextMatches(i -> productId.equals(i.getProductId()))
                .verifyComplete();
    }

    @Test
    void findByProductId_returnsEmpty_whenNotFound() {
        when(repository.findByProductId(productId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByProductId(productId))
                .verifyComplete();
    }

    @Test
    void adjust_returnsUpdatedItem_whenSuccessful() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(inventoryData)).when(fetchSpec).one();

        StepVerifier.create(adapter.adjust(productId, -10))
                .expectNextMatches(i -> productId.equals(i.getProductId()))
                .verifyComplete();
    }

    @Test
    void adjust_propagatesError_whenDatabaseFails() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.error(new RuntimeException("DB error"))).when(fetchSpec).one();

        StepVerifier.create(adapter.adjust(productId, -10))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findByProducerId_returnsItems_whenFound() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Flux.just(inventoryData)).when(fetchSpec).all();

        StepVerifier.create(adapter.findByProducerId(producerId))
                .expectNextMatches(i -> productId.equals(i.getProductId()))
                .verifyComplete();
    }

    @Test
    void findByProducerId_returnsEmpty_whenNone() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Flux.empty()).when(fetchSpec).all();

        StepVerifier.create(adapter.findByProducerId(producerId))
                .verifyComplete();
    }

    @Test
    void findByProductId_propagatesError() {
        when(repository.findByProductId(productId)).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.findByProductId(productId))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findByProducerId_propagatesError() {
        when(databaseClient.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Flux.error(new RuntimeException("DB error"))).when(fetchSpec).all();

        StepVerifier.create(adapter.findByProducerId(producerId))
                .verifyError(RuntimeException.class);
    }
}
