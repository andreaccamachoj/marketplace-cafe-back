package co.com.marketplace.usecase.favorites;

import co.com.marketplace.model.favorites.Favorite;
import co.com.marketplace.model.favorites.gateways.FavoriteGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListFavoritesUseCaseTest {

    @Mock private FavoriteGateway favoriteGateway;

    @InjectMocks
    private ListFavoritesUseCase useCase;

    private final UUID userId = UUID.randomUUID();

    @Test
    void execute_returnsFavorites() {
        Favorite fav = Favorite.builder().id(UUID.randomUUID()).userId(userId)
                .productId(UUID.randomUUID()).addedAt(OffsetDateTime.now()).build();
        when(favoriteGateway.findByUserId(userId)).thenReturn(Flux.just(fav));

        StepVerifier.create(useCase.execute(userId))
                .expectNextMatches(f -> userId.equals(f.getUserId()))
                .verifyComplete();
    }

    @Test
    void execute_returnsEmpty_whenNoFavorites() {
        when(favoriteGateway.findByUserId(userId)).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute(userId))
                .verifyComplete();
    }
}
