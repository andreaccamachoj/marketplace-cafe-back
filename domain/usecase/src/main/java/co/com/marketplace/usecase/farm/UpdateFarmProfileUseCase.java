package co.com.marketplace.usecase.farm;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.farm.Farm;
import co.com.marketplace.model.farm.gateways.FarmGateway;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public final class UpdateFarmProfileUseCase {

    private final FarmGateway farmGateway;
    private final ProducerProfileGateway producerProfileGateway;

    public record Command(
            String name,
            String municipality,
            String department,
            BigDecimal altitudeMasl,
            BigDecimal areaHectares,
            String mainVariety,
            String process,
            String description
    ) {}

    public Mono<Farm> execute(UUID userId, Command cmd) {
        return producerProfileGateway.findByUserId(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("PRODUCER_PROFILE_NOT_FOUND",
                        "Perfil de productor no encontrado para usuario: " + userId)))
                .flatMap(profile -> farmGateway.findByProducerId(profile.getId())
                        .flatMap(existing -> {
                            Farm updated = existing.toBuilder()
                                    .name(cmd.name())
                                    .municipality(cmd.municipality())
                                    .department(cmd.department())
                                    .altitudeMasl(cmd.altitudeMasl())
                                    .areaHectares(cmd.areaHectares())
                                    .mainVariety(cmd.mainVariety())
                                    .process(cmd.process())
                                    .description(cmd.description())
                                    .updatedAt(OffsetDateTime.now())
                                    .build();
                            return farmGateway.update(updated);
                        })
                        .switchIfEmpty(Mono.defer(() -> {
                            Farm newFarm = Farm.builder()
                                    .id(UUID.randomUUID())
                                    .producerId(profile.getId())
                                    .name(cmd.name())
                                    .municipality(cmd.municipality())
                                    .department(cmd.department())
                                    .altitudeMasl(cmd.altitudeMasl())
                                    .areaHectares(cmd.areaHectares())
                                    .mainVariety(cmd.mainVariety())
                                    .process(cmd.process())
                                    .description(cmd.description())
                                    .createdAt(OffsetDateTime.now())
                                    .updatedAt(OffsetDateTime.now())
                                    .build();
                            return farmGateway.save(newFarm);
                        })));
    }
}
