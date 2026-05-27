package co.com.marketplace.r2dbc.payments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentMethodRepositoryAdapterTest {

    @Mock private PaymentMethodReactiveRepository repo;

    @InjectMocks private PaymentMethodRepositoryAdapter adapter;

    private final UUID methodId = UUID.randomUUID();
    private PaymentMethodData methodData;

    @BeforeEach
    void setUp() {
        methodData = PaymentMethodData.builder()
                .id(methodId)
                .code("NEQUI")
                .name("Nequi")
                .type("digital_wallet")
                .accountNumber("3001234567")
                .accountHolder("World Coffee Marketplace")
                .emoji("💚")
                .accentColor("#00B050")
                .isActive(true)
                .displayOrder(1)
                .build();
    }

    @Test
    void findAllActive_returnsMethods_whenFound() {
        when(repo.findAllActive()).thenReturn(Flux.just(methodData));

        StepVerifier.create(adapter.findAllActive())
                .expectNextMatches(m -> "NEQUI".equals(m.getCode()) && m.isActive())
                .verifyComplete();
    }

    @Test
    void findAllActive_returnsEmpty_whenNone() {
        when(repo.findAllActive()).thenReturn(Flux.empty());

        StepVerifier.create(adapter.findAllActive())
                .verifyComplete();
    }

    @Test
    void findById_returnsMethod_whenFound() {
        when(repo.findById(methodId)).thenReturn(Mono.just(methodData));

        StepVerifier.create(adapter.findById(methodId))
                .expectNextMatches(m -> methodId.equals(m.getId()))
                .verifyComplete();
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        when(repo.findById(methodId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById(methodId))
                .verifyComplete();
    }

    @Test
    void findByCode_returnsMethod_whenFound() {
        when(repo.findByCode("NEQUI")).thenReturn(Mono.just(methodData));

        StepVerifier.create(adapter.findByCode("NEQUI"))
                .expectNextMatches(m -> "NEQUI".equals(m.getCode()))
                .verifyComplete();
    }

    @Test
    void findByCode_returnsEmpty_whenNotFound() {
        when(repo.findByCode("UNKNOWN")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByCode("UNKNOWN"))
                .verifyComplete();
    }

    @Test
    void findAllActive_propagatesError() {
        when(repo.findAllActive()).thenReturn(Flux.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.findAllActive())
                .verifyError(RuntimeException.class);
    }

    @Test
    void findById_propagatesError() {
        when(repo.findById(methodId)).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.findById(methodId))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findByCode_propagatesError() {
        when(repo.findByCode("NEQUI")).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.findByCode("NEQUI"))
                .verifyError(RuntimeException.class);
    }
}
