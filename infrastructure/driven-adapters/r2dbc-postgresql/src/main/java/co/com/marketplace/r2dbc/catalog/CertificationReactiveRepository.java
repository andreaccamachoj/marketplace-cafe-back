package co.com.marketplace.r2dbc.catalog;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface CertificationReactiveRepository extends ReactiveCrudRepository<CertificationData, Integer> {
    Mono<CertificationData> findByCode(String code);
}
