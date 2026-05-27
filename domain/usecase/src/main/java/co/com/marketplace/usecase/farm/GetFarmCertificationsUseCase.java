package co.com.marketplace.usecase.farm;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.farm.FarmCertification;
import co.com.marketplace.model.farm.gateways.FarmGateway;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public final class GetFarmCertificationsUseCase {

    private final FarmGateway farmGateway;
    private final ProducerProfileGateway producerProfileGateway;

    public Flux<FarmCertification> execute(UUID userId) {
        return producerProfileGateway.findByUserId(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("PRODUCER_PROFILE_NOT_FOUND",
                        "Perfil de productor no encontrado para usuario: " + userId)))
                .flatMapMany(profile -> farmGateway.findByProducerId(profile.getId())
                        .switchIfEmpty(Mono.error(new NotFoundException("FARM_NOT_FOUND",
                                "Finca no encontrada para el productor: " + profile.getId())))
                        .flatMapMany(farm -> farmGateway.findCertificationsByFarmId(farm.getId())));
    }
}
