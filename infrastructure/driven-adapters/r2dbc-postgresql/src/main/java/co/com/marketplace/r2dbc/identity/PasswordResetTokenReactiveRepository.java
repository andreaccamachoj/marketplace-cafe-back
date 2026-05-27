package co.com.marketplace.r2dbc.identity;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PasswordResetTokenReactiveRepository extends ReactiveCrudRepository<PasswordResetTokenData, UUID> {
    Mono<PasswordResetTokenData> findByTokenHash(String tokenHash);
}
