package co.com.marketplace.model.farm.gateways;

import co.com.marketplace.model.farm.Farm;
import co.com.marketplace.model.farm.FarmCertification;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FarmGateway {
    Mono<Farm> save(Farm farm);
    Mono<Farm> findByProducerId(UUID producerId);
    Mono<Farm> update(Farm farm);
    Mono<FarmCertification> saveCertification(FarmCertification certification);
    Flux<FarmCertification> findCertificationsByFarmId(UUID farmId);
    Mono<Void> deleteCertification(UUID certificationId);
}
