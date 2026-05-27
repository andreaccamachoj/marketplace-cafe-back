package co.com.marketplace.usecase.admin;

import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.identity.User;
import co.com.marketplace.model.identity.UserStatus;
import co.com.marketplace.model.identity.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public final class UnbanUserUseCase {

    private final UserGateway userGateway;

    public Mono<User> execute(UUID userId) {
        return userGateway.findById(userId)
                .switchIfEmpty(Mono.error(new NotFoundException("USER_NOT_FOUND", "User not found: " + userId)))
                .flatMap(user -> {
                    User unbanned = user.toBuilder().status(UserStatus.active).build();
                    return userGateway.update(unbanned);
                });
    }
}
