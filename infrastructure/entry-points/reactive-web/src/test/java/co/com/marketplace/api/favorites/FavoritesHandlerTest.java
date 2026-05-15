package co.com.marketplace.api.favorites;

import co.com.marketplace.api.config.GlobalErrorWebExceptionHandler;
import co.com.marketplace.api.config.TestTxConfig;
import co.com.marketplace.model.favorites.Favorite;
import co.com.marketplace.usecase.favorites.AddFavoriteUseCase;
import co.com.marketplace.usecase.favorites.ListFavoritesUseCase;
import co.com.marketplace.usecase.favorites.RemoveFavoriteUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {FavoritesRouter.class, FavoritesHandler.class, GlobalErrorWebExceptionHandler.class, TestTxConfig.class})
class FavoritesHandlerTest {

    private static final String USER_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Autowired private WebTestClient webTestClient;

    @MockitoBean private ListFavoritesUseCase listFavoritesUseCase;
    @MockitoBean private AddFavoriteUseCase addFavoriteUseCase;
    @MockitoBean private RemoveFavoriteUseCase removeFavoriteUseCase;

    private Favorite buildFavorite() {
        return Favorite.builder()
                .id(UUID.randomUUID()).userId(UUID.fromString(USER_ID))
                .productId(UUID.randomUUID()).addedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void list_returns200() {
        when(listFavoritesUseCase.execute(any())).thenReturn(Flux.just(buildFavorite()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .get().uri("/api/favorites")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void add_returns201() {
        when(addFavoriteUseCase.execute(any(), any())).thenReturn(Mono.just(buildFavorite()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .post().uri("/api/favorites/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void remove_returns204() {
        when(removeFavoriteUseCase.execute(any(), any())).thenReturn(Mono.empty());

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .delete().uri("/api/favorites/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isNoContent();
    }
}
