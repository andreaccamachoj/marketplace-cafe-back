package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.ProducerProfile;
import co.com.marketplace.model.identity.User;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
import co.com.marketplace.model.identity.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public final class GetProducerProfileUseCase {

    private final UserGateway userGateway;
    private final ProducerProfileGateway producerProfileGateway;

    public record Result(UUID id, String email, String fullName, String phone,
                         String bio, String city, String department,
                         String status, String avatarInitials) {}

    public Mono<Result> execute(UUID userId) {
        return Mono.zip(
                userGateway.findById(userId)
                        .switchIfEmpty(Mono.error(new NotFoundException("USER_NOT_FOUND", "Usuario no encontrado"))),
                producerProfileGateway.findByUserId(userId)
                        .switchIfEmpty(Mono.just(ProducerProfile.builder().build()))
        ).map(t -> {
            User u = t.getT1();
            ProducerProfile p = t.getT2();
            return new Result(u.getId(), u.getEmail(), u.getFullName(), u.getPhone(),
                    p.getBio(), p.getCity(), p.getDepartment(),
                    p.getStatus() != null ? p.getStatus().name() : null,
                    p.getAvatarInitials());
        });
    }
}
