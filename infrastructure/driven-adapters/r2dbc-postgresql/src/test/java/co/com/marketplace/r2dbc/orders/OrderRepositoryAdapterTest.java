package co.com.marketplace.r2dbc.orders;

import co.com.marketplace.model.exception.ValidationException;
import co.com.marketplace.model.orders.Order;
import co.com.marketplace.model.orders.OrderStatus;
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

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;
import java.util.function.BiFunction;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
class OrderRepositoryAdapterTest {

    @Mock private OrderReactiveRepository orderRepo;
    @Mock private OrderItemReactiveRepository itemRepo;
    @Mock private DatabaseClient db;
    @Mock private DatabaseClient.GenericExecuteSpec spec;
    @Mock private RowsFetchSpec fetchSpec;

    @InjectMocks private OrderRepositoryAdapter adapter;

    private final UUID orderId = UUID.randomUUID();
    private final UUID buyerId = UUID.randomUUID();
    private OrderData orderData;
    private Order validOrder;

    @BeforeEach
    void setUp() {
        orderData = OrderData.builder()
                .id(orderId)
                .buyerId(buyerId)
                .code("WCM-2024-001")
                .yearlySequence(1)
                .year(2024)
                .subtotal(BigDecimal.valueOf(50000))
                .shippingAmount(BigDecimal.valueOf(8000))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.valueOf(58000))
                .status(OrderStatusType.pending_verification)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        validOrder = Order.builder()
                .id(orderId)
                .buyerId(buyerId)
                .code("WCM-2024-001")
                .yearlySequence(1)
                .year(2024)
                .subtotal(BigDecimal.valueOf(50000))
                .shippingAmount(BigDecimal.valueOf(8000))
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.valueOf(58000))
                .status(OrderStatus.pending_verification)
                .items(Collections.emptyList())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void save_throwsValidationException_whenTotalsDoNotMatch() {
        Order invalidOrder = validOrder.toBuilder()
                .totalAmount(BigDecimal.valueOf(99999))
                .build();

        StepVerifier.create(adapter.save(invalidOrder))
                .verifyError(ValidationException.class);
    }

    @Test
    void findById_returnsOrder_whenFound() {
        when(orderRepo.findById(orderId)).thenReturn(Mono.just(orderData));
        when(itemRepo.findByOrderId(orderId)).thenReturn(Flux.empty());

        StepVerifier.create(adapter.findById(orderId))
                .expectNextMatches(o -> orderId.equals(o.getId()) && "WCM-2024-001".equals(o.getCode()))
                .verifyComplete();
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        when(orderRepo.findById(orderId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById(orderId))
                .verifyComplete();
    }

    @Test
    void findByCode_returnsOrder_whenFound() {
        when(orderRepo.findByCode("WCM-2024-001")).thenReturn(Mono.just(orderData));
        when(itemRepo.findByOrderId(orderId)).thenReturn(Flux.empty());

        StepVerifier.create(adapter.findByCode("WCM-2024-001"))
                .expectNextMatches(o -> "WCM-2024-001".equals(o.getCode()))
                .verifyComplete();
    }

    @Test
    void updateStatus_returnsUpdatedOrder() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(orderData)).when(fetchSpec).one();

        StepVerifier.create(adapter.updateStatus(orderId, OrderStatus.confirmed))
                .expectNextMatches(o -> orderId.equals(o.getId()))
                .verifyComplete();
    }

    @Test
    void findByBuyerId_usesDatabaseClient() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Flux.empty()).when(fetchSpec).all();

        StepVerifier.create(adapter.findByBuyerId(buyerId, null, 0, 10))
                .verifyComplete();
    }

    @Test
    void countByBuyerId_returnsCount() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(3L)).when(fetchSpec).one();

        StepVerifier.create(adapter.countByBuyerId(buyerId, null))
                .expectNext(3L)
                .verifyComplete();
    }

    @Test
    void nextYearlySequence_returnsSequence() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(5)).when(fetchSpec).one();

        StepVerifier.create(adapter.nextYearlySequence(2024))
                .expectNext(5)
                .verifyComplete();
    }

    @Test
    void findByProducerId_usesDatabaseClient() {
        UUID producerId = UUID.randomUUID();
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Flux.empty()).when(fetchSpec).all();

        StepVerifier.create(adapter.findByProducerId(producerId, null, 0, 10))
                .verifyComplete();
    }
}
