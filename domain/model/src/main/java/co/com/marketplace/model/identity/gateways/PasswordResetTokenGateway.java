package co.com.marketplace.model.identity.gateways;

import co.com.marketplace.model.identity.PasswordResetToken;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface PasswordResetTokenGateway {
    Mono<PasswordResetToken> save(PasswordResetToken token);
    Mono<PasswordResetToken> findByTokenHash(String tokenHash);
    Mono<Void> markUsed(UUID tokenId);
}
