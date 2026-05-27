package co.com.marketplace.usecase.admin;

import co.com.marketplace.model.admin.ProducerApproval;
import co.com.marketplace.model.admin.gateways.ProducerApprovalGateway;
import co.com.marketplace.model.identity.ProducerStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListPendingApprovalsUseCaseTest {

    @Mock private ProducerApprovalGateway approvalGateway;

    @InjectMocks
    private ListPendingApprovalsUseCase useCase;

    @Test
    void execute_returnsPendingApprovals() {
        ProducerApproval approval = ProducerApproval.builder().id(UUID.randomUUID())
                .producerId(UUID.randomUUID()).status(ProducerStatus.pending)
                .submittedAt(OffsetDateTime.now()).build();
        when(approvalGateway.findByStatus(ProducerStatus.pending, 0, 10)).thenReturn(Flux.just(approval));

        StepVerifier.create(useCase.execute(0, 10))
                .expectNextMatches(a -> ProducerStatus.pending.equals(a.getStatus()))
                .verifyComplete();
    }

    @Test
    void count_returnsTotalCount() {
        when(approvalGateway.countByStatus(ProducerStatus.pending)).thenReturn(Mono.just(5L));

        StepVerifier.create(useCase.count())
                .expectNext(5L)
                .verifyComplete();
    }
}
