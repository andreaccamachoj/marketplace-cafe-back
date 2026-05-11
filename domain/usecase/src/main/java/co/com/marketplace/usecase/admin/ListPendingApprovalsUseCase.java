package co.com.marketplace.usecase.admin;

import co.com.marketplace.model.admin.ProducerApproval;
import co.com.marketplace.model.admin.gateways.ProducerApprovalGateway;
import co.com.marketplace.model.identity.ProducerStatus;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public final class ListPendingApprovalsUseCase {

    private final ProducerApprovalGateway approvalGateway;

    public Flux<ProducerApproval> execute(int page, int size) {
        return approvalGateway.findByStatus(ProducerStatus.pending, page, size);
    }

    public Mono<Long> count() {
        return approvalGateway.countByStatus(ProducerStatus.pending);
    }

    public Flux<ProducerApproval> executeAll(int page, int size) {
        return approvalGateway.findAll(page, size);
    }
}
