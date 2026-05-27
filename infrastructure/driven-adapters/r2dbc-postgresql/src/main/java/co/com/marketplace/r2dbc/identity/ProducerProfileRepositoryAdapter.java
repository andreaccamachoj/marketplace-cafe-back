package co.com.marketplace.r2dbc.identity;

import co.com.marketplace.model.identity.ProducerProfile;
import co.com.marketplace.model.identity.ProducerStatus;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
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
public class ProducerProfileRepositoryAdapter implements ProducerProfileGateway {

    private final ProducerProfileReactiveRepository repository;
    private final R2dbcEntityTemplate template;

    private static ProducerProfile toDomain(ProducerProfileData d) {
        return ProducerProfile.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .bio(d.getBio())
                .city(d.getCity())
                .department(d.getDepartment())
                .status(ProducerStatus.valueOf(d.getStatus().name()))
                .rejectionReason(d.getRejectionReason())
                .approvedBy(d.getApprovedBy())
                .approvedAt(d.getApprovedAt())
                .avatarInitials(d.getAvatarInitials())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    private static ProducerProfileData toData(ProducerProfile p) {
        return ProducerProfileData.builder()
                .id(p.getId())
                .userId(p.getUserId())
                .bio(p.getBio())
                .city(p.getCity())
                .department(p.getDepartment())
                .status(ProducerStatusType.valueOf(p.getStatus().name()))
                .rejectionReason(p.getRejectionReason())
                .approvedBy(p.getApprovedBy())
                .approvedAt(p.getApprovedAt())
                .avatarInitials(p.getAvatarInitials())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }

    @Override
    public Mono<ProducerProfile> save(ProducerProfile profile) {
        System.out.println();
        System.out.println(profile.toString());
        return repository.save(toData(profile))
                .doOnSubscribe(s -> log.debug("[ProducerProfileRepositoryAdapter#save] DB request: userId={}", profile.getUserId()))
                .doOnSuccess(r -> log.debug("[ProducerProfileRepositoryAdapter#save] DB response: result={}", r != null))
                .doOnError(e -> log.error("[ProducerProfileRepositoryAdapter#save] DB error: {}", e.getMessage()))
                .map(ProducerProfileRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<ProducerProfile> findByUserId(UUID userId) {
        return repository.findByUserId(userId)
                .doOnSubscribe(s -> log.debug("[ProducerProfileRepositoryAdapter#findByUserId] DB request: userId={}", userId))
                .doOnSuccess(r -> log.debug("[ProducerProfileRepositoryAdapter#findByUserId] DB response: found={}", r != null))
                .doOnError(e -> log.error("[ProducerProfileRepositoryAdapter#findByUserId] DB error: {}", e.getMessage()))
                .map(ProducerProfileRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<ProducerProfile> findById(UUID id) {
        return repository.findById(id)
                .doOnSubscribe(s -> log.debug("[ProducerProfileRepositoryAdapter#findById] DB request: id={}", id))
                .doOnSuccess(r -> log.debug("[ProducerProfileRepositoryAdapter#findById] DB response: found={}", r != null))
                .doOnError(e -> log.error("[ProducerProfileRepositoryAdapter#findById] DB error: {}", e.getMessage()))
                .map(ProducerProfileRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<ProducerProfile> update(ProducerProfile profile) {
        return template.update(toData(profile))
                .doOnSubscribe(s -> log.debug("[ProducerProfileRepositoryAdapter#update] DB request: userId={}", profile.getUserId()))
                .doOnSuccess(r -> log.debug("[ProducerProfileRepositoryAdapter#update] DB response: result={}", r != null))
                .doOnError(e -> log.error("[ProducerProfileRepositoryAdapter#update] DB error: {}", e.getMessage()))
                .map(ProducerProfileRepositoryAdapter::toDomain);
    }

    @Override
    public Flux<ProducerProfile> findByStatus(ProducerStatus status, int page, int size) {
        return repository.findByStatus(ProducerStatusType.valueOf(status.name()), size, (long) page * size)
                .doOnSubscribe(s -> log.debug("[ProducerProfileRepositoryAdapter#findByStatus] DB request: status={}, page={}, size={}", status, page, size))
                .doOnComplete(() -> log.debug("[ProducerProfileRepositoryAdapter#findByStatus] DB response: complete"))
                .doOnError(e -> log.error("[ProducerProfileRepositoryAdapter#findByStatus] DB error: {}", e.getMessage()))
                .map(ProducerProfileRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<Long> countByStatus(ProducerStatus status) {
        return repository.countByStatus(ProducerStatusType.valueOf(status.name()))
                .doOnSubscribe(s -> log.debug("[ProducerProfileRepositoryAdapter#countByStatus] DB request: status={}", status))
                .doOnSuccess(r -> log.debug("[ProducerProfileRepositoryAdapter#countByStatus] DB response: result={}", r))
                .doOnError(e -> log.error("[ProducerProfileRepositoryAdapter#countByStatus] DB error: {}", e.getMessage()));
    }
}
