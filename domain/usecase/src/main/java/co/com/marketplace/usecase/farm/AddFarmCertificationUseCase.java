package co.com.marketplace.usecase.farm;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.farm.FarmCertification;
import co.com.marketplace.model.farm.gateways.FarmGateway;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
import co.com.marketplace.model.shared.DocStatus;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

@RequiredArgsConstructor
public final class AddFarmCertificationUseCase {

    private final FarmGateway farmGateway;
    private final ProducerProfileGateway producerProfileGateway;

    public record Command(String type, String name, String issuer, LocalDate expiryDate) {}

    public Mono<FarmCertification> execute(UUID userId, Command cmd) {
        return producerProfileGateway.findByUserId(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("PRODUCER_PROFILE_NOT_FOUND",
                        "Perfil de productor no encontrado para usuario: " + userId)))
                .flatMap(profile -> farmGateway.findByProducerId(profile.getId())
                        .switchIfEmpty(Mono.error(new NotFoundException("FARM_NOT_FOUND",
                                "Finca no encontrada para el productor: " + profile.getId())))
                        .flatMap(farm -> farmGateway.saveCertification(FarmCertification.builder()
                                .id(UUID.randomUUID())
                                .farmId(farm.getId())
                                .certificationId(null)
                                .issuer(cmd.issuer())
                                .expiryDate(cmd.expiryDate())
                                .status(DocStatus.approved)
                                .notes(cmd.type() + "|" + cmd.name())
                                .build())));
    }
}
