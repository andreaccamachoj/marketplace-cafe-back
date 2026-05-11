package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.ProducerProfile;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public final class UpdateProducerProfileUseCase {

    private final ProducerProfileGateway producerProfileGateway;

    public record Command(String bio, String city, String department, String avatarInitials) {}

    public Mono<ProducerProfile> execute(UUID userId, Command cmd) {
        return producerProfileGateway.findByUserId(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("PROFILE_NOT_FOUND",
                        "Perfil de productor no encontrado")))
                .flatMap(profile -> producerProfileGateway.update(profile.toBuilder()
                        .bio(cmd.bio() != null ? cmd.bio() : profile.getBio())
                        .city(cmd.city() != null ? cmd.city() : profile.getCity())
                        .department(cmd.department() != null ? cmd.department() : profile.getDepartment())
                        .avatarInitials(cmd.avatarInitials() != null ? cmd.avatarInitials() : profile.getAvatarInitials())
                        .updatedAt(OffsetDateTime.now())
                        .build()));
    }
}
