package co.com.marketplace.usecase.favorites;

import co.com.marketplace.model.exception.ConflictException;
import co.com.marketplace.model.favorites.Favorite;
import co.com.marketplace.model.favorites.gateways.FavoriteGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddFavoriteUseCaseTest {

    @Mock private FavoriteGateway favoriteGateway;

    @InjectMocks
    private AddFavoriteUseCase addFavoriteUseCase;

    private final UUID userId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();

    @Test
    void execute_savesFavorite_whenNotAlreadyFavorite() {
        Favorite saved = Favorite.builder()
                .id(UUID.randomUUID()).userId(userId).productId(productId).addedAt(OffsetDateTime.now()).build();

        when(favoriteGateway.exists(userId, productId)).thenReturn(Mono.just(false));
        when(favoriteGateway.save(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(addFavoriteUseCase.execute(userId, productId))
                .expectNextMatches(f -> f.getUserId().equals(userId) && f.getProductId().equals(productId))
                .verifyComplete();
    }

    @Test
    void execute_throwsConflict_whenAlreadyFavorite() {
        when(favoriteGateway.exists(userId, productId)).thenReturn(Mono.just(true));

        StepVerifier.create(addFavoriteUseCase.execute(userId, productId))
                .verifyError(ConflictException.class);
    }
}
