package co.com.marketplace.usecase.admin;

import co.com.marketplace.model.admin.ProducerApproval;
import co.com.marketplace.model.admin.gateways.ProducerApprovalGateway;
import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.ProducerProfile;
import co.com.marketplace.model.identity.ProducerStatus;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public final class ApproveProducerUseCase {

    private final ProducerApprovalGateway approvalGateway;
    private final ProducerProfileGateway producerProfileGateway;

    public Mono<ProducerApproval> execute(UUID approvalId, UUID adminId, String notes) {
        return approvalGateway.findById(approvalId)
                .switchIfEmpty(Mono.error(new NotFoundException("APPROVAL_NOT_FOUND", "Approval not found: " + approvalId)))
                .flatMap(approval -> {
                    ProducerApproval updated = approval.toBuilder()
                            .status(ProducerStatus.approved)
                            .reviewedBy(adminId)
                            .reviewedAt(OffsetDateTime.now())
                            .build();
                    return approvalGateway.update(updated)
                            .flatMap(saved -> producerProfileGateway.findByUserId(saved.getProducerId())
                                    .flatMap(profile -> {
                                        ProducerProfile approvedProfile = profile.toBuilder()
                                                .status(ProducerStatus.approved)
                                                .approvedBy(adminId)
                                                .approvedAt(OffsetDateTime.now())
                                                .build();
                                        return producerProfileGateway.update(approvedProfile);
                                    })
                                    .thenReturn(saved));
                });
    }
}
