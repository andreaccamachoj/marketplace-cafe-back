package co.com.marketplace.usecase.farm;

import co.com.marketplace.model.farm.gateways.FarmGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public final class RemoveFarmCertificationUseCase {
    private final FarmGateway farmGateway;

    public Mono<Void> execute(UUID certificationId) {
        return farmGateway.deleteCertification(certificationId);
    }
}
