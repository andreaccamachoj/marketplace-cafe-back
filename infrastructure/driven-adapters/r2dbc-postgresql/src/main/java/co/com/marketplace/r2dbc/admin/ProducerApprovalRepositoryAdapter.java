package co.com.marketplace.r2dbc.admin;

import co.com.marketplace.model.admin.ProducerApproval;
import co.com.marketplace.model.admin.gateways.ProducerApprovalGateway;
import co.com.marketplace.model.identity.ProducerStatus;
import co.com.marketplace.r2dbc.type.ProducerStatusType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProducerApprovalRepositoryAdapter implements ProducerApprovalGateway {

    private final ProducerApprovalReactiveRepository repo;
    private final R2dbcEntityTemplate template;

    @Override
    public Mono<ProducerApproval> save(ProducerApproval approval) {
        return repo.save(toData(approval))
                .doOnSubscribe(s -> log.debug("[ProducerApprovalRepositoryAdapter#save] DB request: producerId={}", approval.getProducerId()))
                .doOnSuccess(r -> log.debug("[ProducerApprovalRepositoryAdapter#save] DB response: result={}", r != null))
                .doOnError(e -> log.error("[ProducerApprovalRepositoryAdapter#save] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    @Override
    public Mono<ProducerApproval> findById(UUID id) {
        return repo.findById(id)
                .doOnSubscribe(s -> log.debug("[ProducerApprovalRepositoryAdapter#findById] DB request: id={}", id))
                .doOnSuccess(r -> log.debug("[ProducerApprovalRepositoryAdapter#findById] DB response: found={}", r != null))
                .doOnError(e -> log.error("[ProducerApprovalRepositoryAdapter#findById] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    @Override
    public Mono<ProducerApproval> findByProducerId(UUID producerId) {
        return repo.findByProducerId(producerId)
                .doOnSubscribe(s -> log.debug("[ProducerApprovalRepositoryAdapter#findByProducerId] DB request: producerId={}", producerId))
                .doOnSuccess(r -> log.debug("[ProducerApprovalRepositoryAdapter#findByProducerId] DB response: found={}", r != null))
                .doOnError(e -> log.error("[ProducerApprovalRepositoryAdapter#findByProducerId] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    @Override
    public Mono<ProducerApproval> update(ProducerApproval approval) {
        return template.update(toData(approval))
                .doOnSubscribe(s -> log.debug("[ProducerApprovalRepositoryAdapter#update] DB request: id={}", approval.getId()))
                .doOnSuccess(r -> log.debug("[ProducerApprovalRepositoryAdapter#update] DB response: result={}", r != null))
                .doOnError(e -> log.error("[ProducerApprovalRepositoryAdapter#update] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    @Override
    public Flux<ProducerApproval> findByStatus(ProducerStatus status, int page, int size) {
        return repo.findByStatus(ProducerStatusType.valueOf(status.name()), size, (long) page * size)
                .doOnSubscribe(s -> log.debug("[ProducerApprovalRepositoryAdapter#findByStatus] DB request: status={}, page={}, size={}", status, page, size))
                .doOnComplete(() -> log.debug("[ProducerApprovalRepositoryAdapter#findByStatus] DB response: complete"))
                .doOnError(e -> log.error("[ProducerApprovalRepositoryAdapter#findByStatus] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    @Override
    public Flux<ProducerApproval> findAll(int page, int size) {
        return repo.findAllPaged(size, (long) page * size)
                .doOnSubscribe(s -> log.debug("[ProducerApprovalRepositoryAdapter#findAll] DB request: page={}, size={}", page, size))
                .doOnComplete(() -> log.debug("[ProducerApprovalRepositoryAdapter#findAll] DB response: complete"))
                .doOnError(e -> log.error("[ProducerApprovalRepositoryAdapter#findAll] DB error: {}", e.getMessage()))
                .map(this::toDomain);
    }

    @Override
    public Mono<Long> countByStatus(ProducerStatus status) {
        return repo.countByStatus(ProducerStatusType.valueOf(status.name()))
                .doOnSubscribe(s -> log.debug("[ProducerApprovalRepositoryAdapter#countByStatus] DB request: status={}", status))
                .doOnSuccess(r -> log.debug("[ProducerApprovalRepositoryAdapter#countByStatus] DB response: result={}", r))
                .doOnError(e -> log.error("[ProducerApprovalRepositoryAdapter#countByStatus] DB error: {}", e.getMessage()));
    }

    private ProducerApproval toDomain(ProducerApprovalData d) {
        return ProducerApproval.builder()
                .id(d.getId())
                .producerId(d.getProducerId())
                .producerNameSnapshot(d.getProducerNameSnapshot())
                .farmNameSnapshot(d.getFarmNameSnapshot())
                .region(d.getRegion())
                .department(d.getDepartment())
                .hectares(d.getHectares())
                .mainVariety(d.getMainVariety())
                .email(d.getEmail())
                .phone(d.getPhone())
                .status(ProducerStatus.valueOf(d.getStatus().name()))
                .rejectionReason(d.getRejectionReason())
                .reviewedBy(d.getReviewedBy())
                .reviewedAt(d.getReviewedAt())
                .submittedAt(d.getSubmittedAt())
                .build();
    }

    private ProducerApprovalData toData(ProducerApproval a) {
        return ProducerApprovalData.builder()
                .id(a.getId())
                .producerId(a.getProducerId())
                .producerNameSnapshot(a.getProducerNameSnapshot())
                .farmNameSnapshot(a.getFarmNameSnapshot())
                .region(a.getRegion())
                .department(a.getDepartment())
                .hectares(a.getHectares())
                .mainVariety(a.getMainVariety())
                .email(a.getEmail())
                .phone(a.getPhone())
                .status(ProducerStatusType.valueOf(a.getStatus().name()))
                .rejectionReason(a.getRejectionReason())
                .reviewedBy(a.getReviewedBy())
                .reviewedAt(a.getReviewedAt())
                .submittedAt(a.getSubmittedAt())
                .build();
    }
}
