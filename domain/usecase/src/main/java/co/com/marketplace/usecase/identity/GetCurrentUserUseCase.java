package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.gateways.ProducerProfileGateway;
import co.com.marketplace.model.identity.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public final class GetCurrentUserUseCase {

    private final UserGateway userGateway;
    private final ProducerProfileGateway producerProfileGateway;

    public record Result(UUID id, String email, String fullName, String phone,
                         String status, String producerStatus, String createdAt) {}

    public Mono<Result> execute(UUID userId) {
        return userGateway.findById(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("USER_NOT_FOUND",
                        "Usuario no encontrado")))
                .flatMap(user -> producerProfileGateway.findByUserId(userId)
                        .map(p -> new Result(
                                user.getId(), user.getEmail(), user.getFullName(), user.getPhone(),
                                user.getStatus().name(),
                                p.getStatus() != null ? p.getStatus().name() : null,
                                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null))
                        .defaultIfEmpty(new Result(
                                user.getId(), user.getEmail(), user.getFullName(), user.getPhone(),
                                user.getStatus().name(), null,
                                user.getCreatedAt() != null ? user.getCreatedAt().toString() : null)));
    }
}
