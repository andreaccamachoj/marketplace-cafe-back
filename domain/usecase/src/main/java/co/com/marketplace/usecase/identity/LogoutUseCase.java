package co.com.marketplace.usecase.identity;

import reactor.core.publisher.Mono;

import java.util.UUID;

public final class LogoutUseCase {

    public Mono<Void> execute(UUID userId) {
        return Mono.empty();
    }
}
