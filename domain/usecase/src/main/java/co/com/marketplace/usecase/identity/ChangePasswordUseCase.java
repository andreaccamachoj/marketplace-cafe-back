package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.exception.ValidationException;
import co.com.marketplace.model.gateway.PasswordEncoderGateway;
import co.com.marketplace.model.identity.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public final class ChangePasswordUseCase {

    private final UserGateway userGateway;
    private final PasswordEncoderGateway passwordEncoder;

    public Mono<Void> execute(UUID userId, String oldPassword, String newPassword) {
        return userGateway.findById(userId)
                .flatMap(user -> Mono.fromCallable(() -> passwordEncoder.matches(oldPassword, user.getHashedPassword()))
                        .filter(Boolean::booleanValue)
                        .switchIfEmpty(Mono.error(new ValidationException("AUTH_WRONG_PASSWORD",
                                "Contraseña actual incorrecta")))
                        .flatMap(__ -> Mono.fromCallable(() -> passwordEncoder.encode(newPassword)))
                        .flatMap(hash -> userGateway.update(user.toBuilder()
                                .hashedPassword(hash)
                                .updatedAt(OffsetDateTime.now())
                                .build())))
                .then();
    }
}
