package co.com.marketplace.r2dbc.orders;

import co.com.marketplace.model.orders.OrderPayment;
import co.com.marketplace.model.orders.PaymentStatus;
import co.com.marketplace.r2dbc.type.PaymentStatusType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.BiFunction;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
class OrderPaymentRepositoryAdapterTest {

    @Mock private OrderPaymentReactiveRepository repo;
    @Mock private DatabaseClient db;
    @Mock private DatabaseClient.GenericExecuteSpec spec;
    @Mock private RowsFetchSpec fetchSpec;

    @InjectMocks private OrderPaymentRepositoryAdapter adapter;

    private final UUID paymentId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final UUID methodId = UUID.randomUUID();
    private OrderPaymentData paymentData;
    private OrderPayment payment;

    @BeforeEach
    void setUp() {
        paymentData = OrderPaymentData.builder()
                .id(paymentId)
                .orderId(orderId)
                .paymentMethodId(methodId)
                .paymentMethodCode("NEQUI")
                .amount(BigDecimal.valueOf(58000))
                .status(PaymentStatusType.submitted)
                .submittedAt(OffsetDateTime.now())
                .build();

        payment = OrderPayment.builder()
                .id(paymentId)
                .orderId(orderId)
                .paymentMethodId(methodId)
                .paymentMethodCode("NEQUI")
                .amount(BigDecimal.valueOf(58000))
                .status(PaymentStatus.submitted)
                .submittedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void save_returnsPayment_whenSuccessful() {
        when(repo.save(any(OrderPaymentData.class))).thenReturn(Mono.just(paymentData));

        StepVerifier.create(adapter.save(payment))
                .expectNextMatches(p -> paymentId.equals(p.getId()) && "NEQUI".equals(p.getPaymentMethodCode()))
                .verifyComplete();
    }

    @Test
    void save_propagatesError_whenRepositoryFails() {
        when(repo.save(any(OrderPaymentData.class))).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.save(payment))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findByOrderId_returnsPayment_whenFound() {
        when(repo.findByOrderId(orderId)).thenReturn(Mono.just(paymentData));

        StepVerifier.create(adapter.findByOrderId(orderId))
                .expectNextMatches(p -> orderId.equals(p.getOrderId()))
                .verifyComplete();
    }

    @Test
    void findByOrderId_returnsEmpty_whenNotFound() {
        when(repo.findByOrderId(orderId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByOrderId(orderId))
                .verifyComplete();
    }

    @Test
    void updateStatus_returnsUpdatedPayment() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        when(spec.bindNull(anyString(), any(Class.class))).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(paymentData)).when(fetchSpec).one();

        UUID verifiedBy = UUID.randomUUID();
        StepVerifier.create(adapter.updateStatus(paymentId, PaymentStatus.verified, verifiedBy))
                .expectNextMatches(p -> paymentId.equals(p.getId()))
                .verifyComplete();
    }
}
