package co.com.marketplace.api.cart;

import co.com.marketplace.usecase.cart.AddCartItemUseCase;
import co.com.marketplace.usecase.cart.ApplyCouponUseCase;
import co.com.marketplace.usecase.cart.ClearCartUseCase;
import co.com.marketplace.usecase.cart.GetCartUseCase;
import co.com.marketplace.usecase.cart.GetCartWithProductsUseCase;
import co.com.marketplace.usecase.cart.RemoveCartItemUseCase;
import co.com.marketplace.usecase.cart.RemoveCouponUseCase;
import co.com.marketplace.usecase.cart.SelectShippingOptionUseCase;
import co.com.marketplace.usecase.cart.UpdateCartItemQuantityUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalOperator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CartHandler {

    private final GetCartUseCase getCartUseCase;
    private final GetCartWithProductsUseCase getCartWithProductsUseCase;
    private final AddCartItemUseCase addCartItemUseCase;
    private final UpdateCartItemQuantityUseCase updateCartItemQuantityUseCase;
    private final RemoveCartItemUseCase removeCartItemUseCase;
    private final ClearCartUseCase clearCartUseCase;
    private final ApplyCouponUseCase applyCouponUseCase;
    private final RemoveCouponUseCase removeCouponUseCase;
    private final SelectShippingOptionUseCase selectShippingOptionUseCase;
    private final TransactionalOperator tx;

    record AddItemRequest(UUID productId, int quantity, BigDecimal unitPriceSnapshot) {}
    record UpdateQuantityRequest(int quantity) {}
    record CouponRequest(String code) {}
    record ShippingRequest(String shippingOptionId) {}

    public Mono<ServerResponse> getCart(ServerRequest request) {
        return userId(request)
                .flatMap(getCartWithProductsUseCase::execute)
                .flatMap(cart -> ServerResponse.ok().bodyValue(cart));
    }

    public Mono<ServerResponse> addItem(ServerRequest request) {
        return userId(request)
                .flatMap(uid -> request.bodyToMono(AddItemRequest.class)
                        .flatMap(body -> addCartItemUseCase.execute(uid,
                                new AddCartItemUseCase.Command(body.productId(), body.quantity(), body.unitPriceSnapshot()))
                                .as(tx::transactional)))
                .flatMap(cart -> ServerResponse.status(HttpStatus.CREATED).bodyValue(cart));
    }

    public Mono<ServerResponse> updateItem(ServerRequest request) {
        UUID itemId = UUID.fromString(request.pathVariable("itemId"));
        return userId(request)
                .flatMap(uid -> request.bodyToMono(UpdateQuantityRequest.class)
                        .flatMap(body -> updateCartItemQuantityUseCase.execute(uid, itemId, body.quantity())))
                .flatMap(cart -> ServerResponse.ok().bodyValue(cart));
    }

    public Mono<ServerResponse> removeItem(ServerRequest request) {
        UUID itemId = UUID.fromString(request.pathVariable("itemId"));
        return userId(request)
                .flatMap(uid -> removeCartItemUseCase.execute(uid, itemId))
                .flatMap(cart -> ServerResponse.ok().bodyValue(cart));
    }

    public Mono<ServerResponse> clearCart(ServerRequest request) {
        return userId(request)
                .flatMap(clearCartUseCase::execute)
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> applyCoupon(ServerRequest request) {
        return userId(request)
                .flatMap(uid -> request.bodyToMono(CouponRequest.class)
                        .flatMap(body -> applyCouponUseCase.execute(uid, body.code())
                                .as(tx::transactional)))
                .flatMap(cart -> ServerResponse.ok().bodyValue(cart));
    }

    public Mono<ServerResponse> removeCoupon(ServerRequest request) {
        return userId(request)
                .flatMap(removeCouponUseCase::execute)
                .flatMap(cart -> ServerResponse.ok().bodyValue(cart));
    }

    public Mono<ServerResponse> selectShipping(ServerRequest request) {
        return userId(request)
                .flatMap(uid -> request.bodyToMono(ShippingRequest.class)
                        .flatMap(body -> selectShippingOptionUseCase.execute(uid, body.shippingOptionId())))
                .flatMap(cart -> ServerResponse.ok().bodyValue(cart));
    }

    private Mono<UUID> userId(ServerRequest request) {
        return request.principal().map(p -> UUID.fromString(p.getName()));
    }
}
