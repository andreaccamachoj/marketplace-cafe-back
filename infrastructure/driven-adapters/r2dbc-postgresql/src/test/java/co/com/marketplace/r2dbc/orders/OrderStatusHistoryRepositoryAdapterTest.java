package co.com.marketplace.r2dbc.orders;

import co.com.marketplace.model.orders.OrderStatus;
import co.com.marketplace.model.orders.OrderStatusHistory;
import co.com.marketplace.r2dbc.type.OrderStatusType;
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
class OrderStatusHistoryRepositoryAdapterTest {

    @Mock private OrderStatusHistoryReactiveRepository repo;
    @Mock private DatabaseClient db;
    @Mock private DatabaseClient.GenericExecuteSpec spec;
    @Mock private RowsFetchSpec fetchSpec;

    @InjectMocks private OrderStatusHistoryRepositoryAdapter adapter;

    private final UUID historyId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private OrderStatusHistoryData historyData;
    private OrderStatusHistory history;

    @BeforeEach
    void setUp() {
        historyData = OrderStatusHistoryData.builder()
                .id(historyId)
                .orderId(orderId)
                .status(OrderStatusType.confirmed)
                .notes("Pago confirmado")
                .changedAt(OffsetDateTime.now())
                .build();

        history = OrderStatusHistory.builder()
                .id(historyId)
                .orderId(orderId)
                .status(OrderStatus.confirmed)
                .notes("Pago confirmado")
                .changedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void save_returnsHistory_whenSuccessful() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        when(spec.bindNull(anyString(), any(Class.class))).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(historyData)).when(fetchSpec).one();

        StepVerifier.create(adapter.save(history))
                .expectNextMatches(h -> orderId.equals(h.getOrderId()) && OrderStatus.confirmed.equals(h.getStatus()))
                .verifyComplete();
    }

    @Test
    void save_propagatesError_whenDatabaseFails() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        when(spec.bindNull(anyString(), any(Class.class))).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.error(new RuntimeException("DB error"))).when(fetchSpec).one();

        StepVerifier.create(adapter.save(history))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findByOrderId_returnsHistory_whenFound() {
        when(repo.findByOrderId(orderId)).thenReturn(Flux.just(historyData));

        StepVerifier.create(adapter.findByOrderId(orderId))
                .expectNextMatches(h -> orderId.equals(h.getOrderId()))
                .verifyComplete();
    }

    @Test
    void findByOrderId_returnsEmpty_whenNone() {
        when(repo.findByOrderId(orderId)).thenReturn(Flux.empty());

        StepVerifier.create(adapter.findByOrderId(orderId))
                .verifyComplete();
    }

    @Test
    void findByOrderId_propagatesError() {
        when(repo.findByOrderId(orderId)).thenReturn(Flux.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.findByOrderId(orderId))
                .verifyError(RuntimeException.class);
    }
}
