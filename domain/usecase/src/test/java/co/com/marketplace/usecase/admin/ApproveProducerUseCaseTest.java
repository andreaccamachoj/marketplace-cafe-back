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
class ApproveProducerUseCaseTest {

    @Mock private ProducerApprovalGateway approvalGateway;
    @Mock private ProducerProfileGateway producerProfileGateway;

    @InjectMocks
    private ApproveProducerUseCase useCase;

    private final UUID approvalId = UUID.randomUUID();
    private final UUID adminId = UUID.randomUUID();
    private final UUID producerId = UUID.randomUUID();

    @Test
    void execute_approvesProducer_whenApprovalFound() {
        ProducerApproval approval = ProducerApproval.builder().id(approvalId)
                .producerId(producerId).status(ProducerStatus.pending)
                .submittedAt(OffsetDateTime.now()).build();
        ProducerApproval updated = approval.toBuilder().status(ProducerStatus.approved)
                .reviewedBy(adminId).reviewedAt(OffsetDateTime.now()).build();
        ProducerProfile profile = ProducerProfile.builder().id(UUID.randomUUID()).userId(producerId)
                .status(ProducerStatus.pending).build();
        ProducerProfile approvedProfile = profile.toBuilder().status(ProducerStatus.approved).build();

        when(approvalGateway.findById(approvalId)).thenReturn(Mono.just(approval));
        when(approvalGateway.update(any())).thenReturn(Mono.just(updated));
        when(producerProfileGateway.findByUserId(producerId)).thenReturn(Mono.just(profile));
        when(producerProfileGateway.update(any())).thenReturn(Mono.just(approvedProfile));

        StepVerifier.create(useCase.execute(approvalId, adminId, "Looks good"))
                .expectNextMatches(a -> ProducerStatus.approved.equals(a.getStatus()))
                .verifyComplete();
    }

    @Test
    void execute_throwsNotFound_whenApprovalMissing() {
        when(approvalGateway.findById(approvalId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(approvalId, adminId, "notes"))
                .verifyError(NotFoundException.class);
    }
}
