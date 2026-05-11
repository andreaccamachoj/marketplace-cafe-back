package co.com.marketplace.api.favorites;

import co.com.marketplace.usecase.favorites.AddFavoriteUseCase;
import co.com.marketplace.usecase.favorites.ListFavoritesUseCase;
import co.com.marketplace.usecase.favorites.RemoveFavoriteUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class FavoritesHandler {

    private final ListFavoritesUseCase listFavoritesUseCase;
    private final AddFavoriteUseCase addFavoriteUseCase;
    private final RemoveFavoriteUseCase removeFavoriteUseCase;

    public Mono<ServerResponse> list(ServerRequest request) {
        return userId(request)
                .flatMapMany(listFavoritesUseCase::execute)
                .collectList()
                .flatMap(list -> ServerResponse.ok().bodyValue(list));
    }

    public Mono<ServerResponse> add(ServerRequest request) {
        UUID productId = UUID.fromString(request.pathVariable("productId"));
        return userId(request)
                .flatMap(uid -> addFavoriteUseCase.execute(uid, productId))
                .then(ServerResponse.status(HttpStatus.CREATED).build());
    }

    public Mono<ServerResponse> remove(ServerRequest request) {
        UUID productId = UUID.fromString(request.pathVariable("productId"));
        return userId(request)
                .flatMap(uid -> removeFavoriteUseCase.execute(uid, productId))
                .then(ServerResponse.noContent().build());
    }

    private Mono<UUID> userId(ServerRequest request) {
        return request.principal().map(p -> UUID.fromString(p.getName()));
    }
}
