package co.com.marketplace.r2dbc.catalog;

import co.com.marketplace.model.catalog.Certification;
import co.com.marketplace.model.catalog.gateways.CertificationGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class CertificationRepositoryAdapter implements CertificationGateway {

    private final CertificationReactiveRepository repository;

    @Override
    public Flux<Certification> findAll() {
        return repository.findAll()
                .doOnSubscribe(s -> log.debug("[CertificationRepositoryAdapter#findAll] DB request"))
                .doOnComplete(() -> log.debug("[CertificationRepositoryAdapter#findAll] DB response: complete"))
                .doOnError(e -> log.error("[CertificationRepositoryAdapter#findAll] DB error: {}", e.getMessage()))
                .map(CertificationRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<Certification> findByCode(String code) {
        return repository.findByCode(code)
                .doOnSubscribe(s -> log.debug("[CertificationRepositoryAdapter#findByCode] DB request: code={}", code))
                .doOnSuccess(r -> log.debug("[CertificationRepositoryAdapter#findByCode] DB response: found={}", r != null))
                .doOnError(e -> log.error("[CertificationRepositoryAdapter#findByCode] DB error: {}", e.getMessage()))
                .map(CertificationRepositoryAdapter::toDomain);
    }

    static Certification toDomain(CertificationData d) {
        return Certification.builder()
                .id(d.getId())
                .code(d.getCode())
                .name(d.getName())
                .issuingBody(d.getIssuingBody())
                .description(d.getDescription())
                .build();
    }
}
