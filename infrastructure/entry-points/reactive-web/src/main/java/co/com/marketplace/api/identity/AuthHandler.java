package co.com.marketplace.api.identity;

import co.com.marketplace.usecase.identity.ChangePasswordUseCase;
import co.com.marketplace.usecase.identity.ConfirmPasswordResetUseCase;
import co.com.marketplace.usecase.identity.GetCurrentUserUseCase;
import co.com.marketplace.usecase.identity.LoginUseCase;
import co.com.marketplace.usecase.identity.LogoutUseCase;
import co.com.marketplace.usecase.identity.RecordPrivacyConsentUseCase;
import co.com.marketplace.usecase.identity.RefreshTokenUseCase;
import co.com.marketplace.usecase.identity.RegisterBuyerUseCase;
import co.com.marketplace.usecase.identity.RegisterProducerUseCase;
import co.com.marketplace.usecase.identity.RequestPasswordResetUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthHandler {

    private final RegisterBuyerUseCase registerBuyerUseCase;
    private final RegisterProducerUseCase registerProducerUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final LogoutUseCase logoutUseCase;
    private final RequestPasswordResetUseCase requestPasswordResetUseCase;
    private final ConfirmPasswordResetUseCase confirmPasswordResetUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final RecordPrivacyConsentUseCase recordPrivacyConsentUseCase;
    private final TransactionalOperator tx;

    record RegisterBuyerRequest(String email, String password, String fullName, String phone) {}
    record RegisterProducerRequest(String email, String password, String fullName, String phone,
                                   String bio, String city, String department) {}
    record LoginRequest(String email, String password) {}
    record RefreshRequest(String refreshToken) {}
    record PasswordResetRequestBody(String email) {}
    record ConfirmPasswordResetBody(String token, String newPassword) {}
    record ChangePasswordBody(String oldPassword, String newPassword) {}
    record PrivacyConsentBody(String policyVersion) {}

    public Mono<ServerResponse> registerBuyer(ServerRequest request) {
        return request.bodyToMono(RegisterBuyerRequest.class)
                .flatMap(body -> registerBuyerUseCase.execute(
                        new RegisterBuyerUseCase.Command(body.email(), body.password(), body.fullName(), body.phone()))
                        .as(tx::transactional))
                .flatMap(tokens -> ServerResponse.status(HttpStatus.CREATED).bodyValue(tokens));
    }

    public Mono<ServerResponse> registerProducer(ServerRequest request) {
        return request.bodyToMono(RegisterProducerRequest.class)
                .flatMap(body -> registerProducerUseCase.execute(
                        new RegisterProducerUseCase.Command(body.email(), body.password(), body.fullName(),
                                body.phone(), body.bio(), body.city(), body.department()))
                        .as(tx::transactional))
                .flatMap(tokens -> ServerResponse.status(HttpStatus.CREATED).bodyValue(tokens));
    }

    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(LoginRequest.class)
                .flatMap(body -> loginUseCase.execute(body.email(), body.password()))
                .flatMap(tokens -> ServerResponse.ok().bodyValue(tokens));
    }

    public Mono<ServerResponse> refresh(ServerRequest request) {
        return request.bodyToMono(RefreshRequest.class)
                .flatMap(body -> refreshTokenUseCase.execute(body.refreshToken()))
                .flatMap(tokens -> ServerResponse.ok().bodyValue(tokens));
    }

    public Mono<ServerResponse> logout(ServerRequest request) {
        return userId(request)
                .flatMap(logoutUseCase::execute)
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> requestPasswordReset(ServerRequest request) {
        return request.bodyToMono(PasswordResetRequestBody.class)
                .flatMap(body -> requestPasswordResetUseCase.execute(body.email())
                        .as(tx::transactional))
                .then(ServerResponse.accepted().build());
    }

    public Mono<ServerResponse> confirmPasswordReset(ServerRequest request) {
        return request.bodyToMono(ConfirmPasswordResetBody.class)
                .flatMap(body -> confirmPasswordResetUseCase.execute(body.token(), body.newPassword())
                        .as(tx::transactional))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> me(ServerRequest request) {
        return userId(request)
                .flatMap(getCurrentUserUseCase::execute)
                .flatMap(user -> ServerResponse.ok().bodyValue(user));
    }

    public Mono<ServerResponse> changePassword(ServerRequest request) {
        return userId(request)
                .flatMap(uid -> request.bodyToMono(ChangePasswordBody.class)
                        .flatMap(body -> changePasswordUseCase.execute(uid, body.oldPassword(), body.newPassword())))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> recordConsent(ServerRequest request) {
        String ip = request.remoteAddress().map(a -> a.getAddress().getHostAddress()).orElse(null);
        return userId(request)
                .flatMap(uid -> request.bodyToMono(PrivacyConsentBody.class)
                        .flatMap(body -> recordPrivacyConsentUseCase.execute(uid, body.policyVersion(), ip)
                                .as(tx::transactional)))
                .then(ServerResponse.noContent().build());
    }

    private Mono<UUID> userId(ServerRequest request) {
        return request.principal().map(p -> UUID.fromString(p.getName()));
    }
}
