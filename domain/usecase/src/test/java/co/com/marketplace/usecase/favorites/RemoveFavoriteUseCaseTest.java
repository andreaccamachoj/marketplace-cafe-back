package co.com.marketplace.usecase.favorites;

import co.com.marketplace.model.favorites.gateways.FavoriteGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RemoveFavoriteUseCaseTest {

    @Mock private FavoriteGateway favoriteGateway;

    @InjectMocks
    private RemoveFavoriteUseCase useCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @Test
    void execute_deletesSuccessfully() {
        when(favoriteGateway.delete(userId, productId)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(userId, productId))
                .verifyComplete();

        verify(favoriteGateway).delete(userId, productId);
    }

    @Test
    void execute_propagatesError_whenGatewayFails() {
        when(favoriteGateway.delete(userId, productId)).thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(useCase.execute(userId, productId))
                .verifyError(RuntimeException.class);
    }
}
