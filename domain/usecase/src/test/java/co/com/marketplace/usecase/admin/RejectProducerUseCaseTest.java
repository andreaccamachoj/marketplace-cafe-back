package co.com.marketplace.usecase.admin;

import co.com.marketplace.model.admin.ProducerApproval;
import co.com.marketplace.model.admin.gateways.ProducerApprovalGateway;
import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.ProducerProfile;
import co.com.marketplace.model.identity.ProducerStatus;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RejectProducerUseCaseTest {

    @Mock private ProducerApprovalGateway approvalGateway;
    @Mock private ProducerProfileGateway producerProfileGateway;

    @InjectMocks
    private RejectProducerUseCase useCase;

    private final UUID approvalId = UUID.randomUUID();
    private final UUID adminId = UUID.randomUUID();
    private final UUID producerId = UUID.randomUUID();

    @Test
    void execute_rejectsProducer_whenApprovalFound() {
        ProducerApproval approval = ProducerApproval.builder().id(approvalId)
                .producerId(producerId).status(ProducerStatus.pending)
                .submittedAt(OffsetDateTime.now()).build();
        ProducerApproval rejected = approval.toBuilder().status(ProducerStatus.rejected)
                .rejectionReason("Incomplete docs").reviewedBy(adminId).reviewedAt(OffsetDateTime.now()).build();
        ProducerProfile profile = ProducerProfile.builder().id(UUID.randomUUID()).userId(producerId)
                .status(ProducerStatus.pending).build();
        ProducerProfile rejectedProfile = profile.toBuilder().status(ProducerStatus.rejected).build();

        when(approvalGateway.findById(approvalId)).thenReturn(Mono.just(approval));
        when(approvalGateway.update(any())).thenReturn(Mono.just(rejected));
        when(producerProfileGateway.findByUserId(producerId)).thenReturn(Mono.just(profile));
        when(producerProfileGateway.update(any())).thenReturn(Mono.just(rejectedProfile));

        StepVerifier.create(useCase.execute(approvalId, adminId, "Incomplete docs"))
                .expectNextMatches(a -> ProducerStatus.rejected.equals(a.getStatus()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenApprovalMissing() {
        when(approvalGateway.findById(approvalId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(approvalId, adminId, "reason"))
                .verifyError(NotFoundException.class);
    }
}
