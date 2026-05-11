package co.com.marketplace.usecase.admin;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.User;
import co.com.marketplace.model.identity.UserStatus;
import co.com.marketplace.model.identity.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public final class BanUserUseCase {

    private final UserGateway userGateway;

    public Mono<User> execute(UUID userId, String reason) {
        return userGateway.findById(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("USER_NOT_FOUND", "User not found: " + userId)))
                .flatMap(user -> {
                    User banned = user.toBuilder().status(UserStatus.banned).build();
                    return userGateway.update(banned);
                });
    }
}
