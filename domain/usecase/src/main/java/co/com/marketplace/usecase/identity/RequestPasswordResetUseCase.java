package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.identity.PasswordResetToken;
import co.com.marketplace.model.identity.gateways.PasswordResetTokenGateway;
import co.com.marketplace.model.identity.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public final class RequestPasswordResetUseCase {

    private final UserGateway userGateway;
    private final PasswordResetTokenGateway tokenGateway;

    public Mono<String> execute(String email) {
        return userGateway.findByEmail(email)
                .flatMap(user -> {
                    String rawToken = UUID.randomUUID().toString().replace("-", "");
                    String tokenHash = sha256(rawToken);
                    PasswordResetToken token = PasswordResetToken.builder()
                            .userId(user.getId())
                            .tokenHash(tokenHash)
                            .expiresAt(OffsetDateTime.now().plusHours(1))
                            .createdAt(OffsetDateTime.now())
                            .build();
                    return tokenGateway.save(token).thenReturn(rawToken);
                })
                .switchIfEmpty(Mono.just(""));
    }

    private static String sha256(String input) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
