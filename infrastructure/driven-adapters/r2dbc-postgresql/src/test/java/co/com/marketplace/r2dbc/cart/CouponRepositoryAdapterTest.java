package co.com.marketplace.r2dbc.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponRepositoryAdapterTest {

    @Mock private CouponReactiveRepository repo;
    @Mock private DatabaseClient db;
    @Mock private DatabaseClient.GenericExecuteSpec spec;

    @InjectMocks private CouponRepositoryAdapter adapter;

    private CouponData couponData;

    @BeforeEach
    void setUp() {
        couponData = CouponData.builder()
                .id(1)
                .code("DESCUENTO10")
                .description("10% de descuento")
                .discountType(co.com.marketplace.r2dbc.type.CouponDiscountType.percentage)
                .discountValue(BigDecimal.TEN)
                .minSubtotal(BigDecimal.valueOf(50000))
                .usageLimit(100)
                .usedCount(5)
                .validFrom(OffsetDateTime.now().minusDays(1))
                .validUntil(OffsetDateTime.now().plusDays(30))
                .isActive(true)
                .build();
    }

    @Test
    void findByCode_returnsCoupon_whenFound() {
        when(repo.findByCode("DESCUENTO10")).thenReturn(Mono.just(couponData));

        StepVerifier.create(adapter.findByCode("DESCUENTO10"))
                .expectNextMatches(c -> "DESCUENTO10".equals(c.getCode()) && c.isActive())
                .verifyComplete();
    }

    @Test
    void findByCode_returnsEmpty_whenNotFound() {
        when(repo.findByCode("INVALID")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByCode("INVALID"))
                .verifyComplete();
    }

    @Test
    void incrementUsage_completesSuccessfully() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        when(spec.then()).thenReturn(Mono.empty());

        StepVerifier.create(adapter.incrementUsage(1))
                .verifyComplete();
    }

    @Test
    void incrementUsage_propagatesError() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        when(spec.then()).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.incrementUsage(1))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findByCode_propagatesError() {
        when(repo.findByCode("DESCUENTO10")).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.findByCode("DESCUENTO10"))
                .verifyError(RuntimeException.class);
    }
}
