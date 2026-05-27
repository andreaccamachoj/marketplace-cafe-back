package co.com.marketplace.usecase.farm;

import co.com.marketplace.model.farm.FarmCertification;
import co.com.marketplace.model.farm.gateways.FarmGateway;
import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.shared.DocStatus;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.UUID;

@RequiredArgsConstructor
public final class LinkFarmCertificationUseCase {

    private final FarmGateway farmGateway;

    public record Command(
            Integer certificationId,
            String issuer,
            LocalDate issueDate,
            LocalDate expiryDate,
            String documentUrl
    ) {}

    public Mono<FarmCertification> execute(UUID producerId, Command cmd) {
        return farmGateway.findByProducerId(producerId)
                .switchIfEmpty(Mono.error(new NotFoundException("FARM_NOT_FOUND", "Farm not found for producer: " + producerId)))
                .flatMap(farm -> {
                    FarmCertification cert = FarmCertification.builder()
                            .id(UUID.randomUUID())
                            .farmId(farm.getId())
                            .certificationId(cmd.certificationId())
                            .issuer(cmd.issuer())
                            .issueDate(cmd.issueDate())
                            .expiryDate(cmd.expiryDate())
                            .documentUrl(cmd.documentUrl())
                            .status(DocStatus.pending)
                            .build();
                    return farmGateway.saveCertification(cert);
                });
    }
}
