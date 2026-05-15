package co.com.marketplace.r2dbc.admin;

import co.com.marketplace.model.admin.ProducerApproval;
import co.com.marketplace.model.identity.ProducerStatus;
import co.com.marketplace.r2dbc.type.ProducerStatusType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProducerApprovalRepositoryAdapterTest {

    @Mock private ProducerApprovalReactiveRepository repo;
    @Mock private R2dbcEntityTemplate template;

    @InjectMocks private ProducerApprovalRepositoryAdapter adapter;

    private final UUID approvalId = UUID.randomUUID();
    private final UUID producerId = UUID.randomUUID();
    private ProducerApprovalData approvalData;
    private ProducerApproval approval;

    @BeforeEach
    void setUp() {
        approvalData = ProducerApprovalData.builder()
                .id(approvalId)
                .producerId(producerId)
                .producerNameSnapshot("Juan Productor")
                .farmNameSnapshot("Finca La Paz")
                .region("Eje Cafetero")
                .department("Caldas")
                .status(ProducerStatusType.pending)
                .submittedAt(OffsetDateTime.now())
                .build();

        approval = ProducerApproval.builder()
                .id(approvalId)
                .producerId(producerId)
                .producerNameSnapshot("Juan Productor")
                .farmNameSnapshot("Finca La Paz")
                .region("Eje Cafetero")
                .department("Caldas")
                .status(ProducerStatus.pending)
                .submittedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void save_returnsApproval_whenSuccessful() {
        when(repo.save(any(ProducerApprovalData.class))).thenReturn(Mono.just(approvalData));

        StepVerifier.create(adapter.save(approval))
                .expectNextMatches(a -> approvalId.equals(a.getId()) && producerId.equals(a.getProducerId()))
                .verifyComplete();
    }

    @Test
    void save_propagatesError_whenRepositoryFails() {
        when(repo.save(any(ProducerApprovalData.class))).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.save(approval))
                .verifyError(RuntimeException.class);
    }

    @Test
    void findById_returnsApproval_whenFound() {
        when(repo.findById(approvalId)).thenReturn(Mono.just(approvalData));

        StepVerifier.create(adapter.findById(approvalId))
                .expectNextMatches(a -> approvalId.equals(a.getId()))
                .verifyComplete();
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        when(repo.findById(approvalId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById(approvalId))
                .verifyComplete();
    }

    @Test
    void findByProducerId_returnsApproval_whenFound() {
        when(repo.findByProducerId(producerId)).thenReturn(Mono.just(approvalData));

        StepVerifier.create(adapter.findByProducerId(producerId))
                .expectNextMatches(a -> producerId.equals(a.getProducerId()))
                .verifyComplete();
    }

    @Test
    void findByProducerId_returnsEmpty_whenNotFound() {
        when(repo.findByProducerId(producerId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByProducerId(producerId))
                .verifyComplete();
    }

    @Test
    void update_returnsUpdatedApproval_whenSuccessful() {
        when(template.update(any(ProducerApprovalData.class))).thenReturn(Mono.just(approvalData));

        StepVerifier.create(adapter.update(approval))
                .expectNextMatches(a -> approvalId.equals(a.getId()))
                .verifyComplete();
    }

    @Test
    void findByStatus_returnsList_whenFound() {
        when(repo.findByStatus(ProducerStatusType.pending, 10, 0L))
                .thenReturn(Flux.just(approvalData));

        StepVerifier.create(adapter.findByStatus(ProducerStatus.pending, 0, 10))
                .expectNextMatches(a -> ProducerStatus.pending.equals(a.getStatus()))
                .verifyComplete();
    }

    @Test
    void findAll_returnsList_whenFound() {
        when(repo.findAllPaged(10, 0L)).thenReturn(Flux.just(approvalData));

        StepVerifier.create(adapter.findAll(0, 10))
                .expectNextMatches(a -> approvalId.equals(a.getId()))
                .verifyComplete();
    }

    @Test
    void countByStatus_returnsCount() {
        when(repo.countByStatus(ProducerStatusType.pending)).thenReturn(Mono.just(5L));

        StepVerifier.create(adapter.countByStatus(ProducerStatus.pending))
                .expectNext(5L)
                .verifyComplete();
    }
}
