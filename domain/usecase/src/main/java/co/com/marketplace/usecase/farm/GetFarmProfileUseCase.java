package co.com.marketplace.usecase.farm;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.farm.Farm;
import co.com.marketplace.model.farm.gateways.FarmGateway;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
import reactor.core.publisher.Mono;

import java.util.UUID;

public final class GetFarmProfileUseCase {

    private final FarmGateway farmGateway;
    private final ProducerProfileGateway producerProfileGateway;

    public GetFarmProfileUseCase(FarmGateway farmGateway, ProducerProfileGateway producerProfileGateway) {
        this.farmGateway = farmGateway;
        this.producerProfileGateway = producerProfileGateway;
    }

    public Mono<Farm> execute(UUID userId) {
        return producerProfileGateway.findByUserId(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("PRODUCER_PROFILE_NOT_FOUND",
                        "Perfil de productor no encontrado para usuario: " + userId)))
                .flatMap(profile -> farmGateway.findByProducerId(profile.getId())
                        .switchIfEmpty(Mono.error(new NotFoundException("FARM_NOT_FOUND",
                                "Finca no encontrada para el productor: " + profile.getId()))));
    }
}
