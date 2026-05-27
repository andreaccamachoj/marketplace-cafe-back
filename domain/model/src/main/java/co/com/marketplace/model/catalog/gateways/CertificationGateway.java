package co.com.marketplace.model.catalog.gateways;

import co.com.marketplace.model.catalog.Certification;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CertificationGateway {
    Flux<Certification> findAll();
    Mono<Certification> findByCode(String code);
}
