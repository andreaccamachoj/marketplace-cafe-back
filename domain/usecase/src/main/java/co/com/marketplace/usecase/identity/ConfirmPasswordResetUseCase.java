package co.com.marketplace.usecase.identity;

import co.com.marketplace.model.exception.ValidationException;
import co.com.marketplace.model.gateway.PasswordEncoderGateway;
import co.com.marketplace.model.identity.gateways.PasswordResetTokenGateway;
import co.com.marketplace.model.identity.gateways.UserGateway;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.OffsetDateTime;

@RequiredArgsConstructor
public final class ConfirmPasswordResetUseCase {

    private final PasswordResetTokenGateway tokenGateway;
    private final UserGateway userGateway;
    private final PasswordEncoderGateway passwordEncoder;

    public Mono<Void> execute(String rawToken, String newPassword) {
        String tokenHash = sha256(rawToken);
        return tokenGateway.findByTokenHash(tokenHash)
                .switchIfEmpty(Mono.error(new ValidationException("RESET_TOKEN_INVALID",
                        "Token de restablecimiento inválido")))
                .flatMap(token -> {
                    if (token.getUsedAt() != null) {
                        return Mono.error(new ValidationException("RESET_TOKEN_USED",
                                "El token ya fue utilizado"));
                    }
                    if (token.getExpiresAt().isBefore(OffsetDateTime.now())) {
                        return Mono.error(new ValidationException("RESET_TOKEN_EXPIRED",
                                "El token ha expirado"));
                    }
                    return userGateway.findById(token.getUserId())
                            .flatMap(user -> Mono.fromCallable(() -> passwordEncoder.encode(newPassword))
                                    .flatMap(hash -> userGateway.update(user.toBuilder()
                                            .hashedPassword(hash)
                                            .updatedAt(OffsetDateTime.now())
                                            .build()))
                                    .then(tokenGateway.markUsed(token.getId())));
                });
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
