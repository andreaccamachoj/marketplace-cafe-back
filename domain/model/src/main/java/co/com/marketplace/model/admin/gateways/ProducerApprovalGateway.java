package co.com.marketplace.model.admin.gateways;

import co.com.marketplace.model.admin.ProducerApproval;
import co.com.marketplace.model.identity.ProducerStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProducerApprovalGateway {
    Mono<ProducerApproval> save(ProducerApproval approval);
    Mono<ProducerApproval> findById(UUID id);
    Mono<ProducerApproval> findByProducerId(UUID producerId);
    Mono<ProducerApproval> update(ProducerApproval approval);
    Flux<ProducerApproval> findByStatus(ProducerStatus status, int page, int size);
    Mono<Long> countByStatus(ProducerStatus status);
    Flux<ProducerApproval> findAll(int page, int size);
}
