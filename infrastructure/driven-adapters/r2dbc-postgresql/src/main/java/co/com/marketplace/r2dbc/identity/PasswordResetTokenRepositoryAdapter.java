package co.com.marketplace.r2dbc.identity;

import co.com.marketplace.model.identity.PasswordResetToken;
import co.com.marketplace.model.identity.gateways.PasswordResetTokenGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordResetTokenRepositoryAdapter implements PasswordResetTokenGateway {

    private final PasswordResetTokenReactiveRepository repository;
    private final DatabaseClient databaseClient;

    private static PasswordResetToken toDomain(PasswordResetTokenData d) {
        return PasswordResetToken.builder()
                .id(d.getId())
                .userId(d.getUserId())
                .tokenHash(d.getTokenHash())
                .expiresAt(d.getExpiresAt())
                .usedAt(d.getUsedAt())
                .createdAt(d.getCreatedAt())
                .build();
    }

    private static PasswordResetTokenData toData(PasswordResetToken t) {
        return PasswordResetTokenData.builder()
                .id(t.getId())
                .userId(t.getUserId())
                .tokenHash(t.getTokenHash())
                .expiresAt(t.getExpiresAt())
                .usedAt(t.getUsedAt())
                .createdAt(t.getCreatedAt())
                .build();
    }

    @Override
    public Mono<PasswordResetToken> save(PasswordResetToken token) {
        return repository.save(toData(token))
                .doOnSubscribe(s -> log.debug("[PasswordResetTokenRepositoryAdapter#save] DB request: userId={}", token.getUserId()))
                .doOnSuccess(r -> log.debug("[PasswordResetTokenRepositoryAdapter#save] DB response: result={}", r != null))
                .doOnError(e -> log.error("[PasswordResetTokenRepositoryAdapter#save] DB error: {}", e.getMessage()))
                .map(PasswordResetTokenRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<PasswordResetToken> findByTokenHash(String tokenHash) {
        return repository.findByTokenHash(tokenHash)
                .doOnSubscribe(s -> log.debug("[PasswordResetTokenRepositoryAdapter#findByTokenHash] DB request: tokenHash={}", tokenHash))
                .doOnSuccess(r -> log.debug("[PasswordResetTokenRepositoryAdapter#findByTokenHash] DB response: found={}", r != null))
                .doOnError(e -> log.error("[PasswordResetTokenRepositoryAdapter#findByTokenHash] DB error: {}", e.getMessage()))
                .map(PasswordResetTokenRepositoryAdapter::toDomain);
    }

    @Override
    public Mono<Void> markUsed(UUID tokenId) {
        return databaseClient.sql(
                        "UPDATE marketplace.password_reset_tokens SET used_at = NOW() WHERE id = :id")
                .bind("id", tokenId)
                .then()
                .doOnSubscribe(s -> log.debug("[PasswordResetTokenRepositoryAdapter#markUsed] DB request: tokenId={}", tokenId))
                .doOnTerminate(() -> log.debug("[PasswordResetTokenRepositoryAdapter#markUsed] DB response: done"))
                .doOnError(e -> log.error("[PasswordResetTokenRepositoryAdapter#markUsed] DB error: {}", e.getMessage()));
    }
}
