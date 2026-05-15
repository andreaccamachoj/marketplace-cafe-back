package co.com.marketplace.api.cart;

import co.com.marketplace.api.config.GlobalErrorWebExceptionHandler;
import co.com.marketplace.api.config.TestTxConfig;
import co.com.marketplace.usecase.cart.AddCartItemUseCase;
import co.com.marketplace.usecase.cart.ApplyCouponUseCase;
import co.com.marketplace.usecase.cart.ClearCartUseCase;
import co.com.marketplace.usecase.cart.GetCartUseCase;
import co.com.marketplace.usecase.cart.GetCartWithProductsUseCase;
import co.com.marketplace.usecase.cart.RemoveCartItemUseCase;
import co.com.marketplace.usecase.cart.RemoveCouponUseCase;
import co.com.marketplace.usecase.cart.SelectShippingOptionUseCase;
import co.com.marketplace.usecase.cart.UpdateCartItemQuantityUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {CartRouter.class, CartHandler.class, GlobalErrorWebExceptionHandler.class, TestTxConfig.class})
class CartHandlerTest {

    private static final String USER_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Autowired private WebTestClient webTestClient;

    @MockitoBean private GetCartUseCase getCartUseCase;
    @MockitoBean private GetCartWithProductsUseCase getCartWithProductsUseCase;
    @MockitoBean private AddCartItemUseCase addCartItemUseCase;
    @MockitoBean private UpdateCartItemQuantityUseCase updateCartItemQuantityUseCase;
    @MockitoBean private RemoveCartItemUseCase removeCartItemUseCase;
    @MockitoBean private ClearCartUseCase clearCartUseCase;
    @MockitoBean private ApplyCouponUseCase applyCouponUseCase;
    @MockitoBean private RemoveCouponUseCase removeCouponUseCase;
    @MockitoBean private SelectShippingOptionUseCase selectShippingOptionUseCase;

    private GetCartWithProductsUseCase.CartDetail buildCartDetail() {
        return new GetCartWithProductsUseCase.CartDetail(UUID.randomUUID(), List.of(), null, null);
    }

    @Test
    void getCart_returns200() {
        when(getCartWithProductsUseCase.execute(any())).thenReturn(Mono.just(buildCartDetail()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .get().uri("/api/cart")
                .exchange()
                .expectStatus().isOk();
    }


    @Test
    void updateItem_returns200() {
        when(updateCartItemQuantityUseCase.execute(any(), any(), anyInt())).thenReturn(Mono.empty());
        when(getCartWithProductsUseCase.execute(any())).thenReturn(Mono.just(buildCartDetail()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .patch().uri("/api/cart/items/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"quantity":3}
                        """)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void removeItem_returns200() {
        when(removeCartItemUseCase.execute(any(), any())).thenReturn(Mono.empty());
        when(getCartWithProductsUseCase.execute(any())).thenReturn(Mono.just(buildCartDetail()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .delete().uri("/api/cart/items/" + UUID.randomUUID())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void clearCart_returns204() {
        when(clearCartUseCase.execute(any())).thenReturn(Mono.empty());

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .delete().uri("/api/cart")
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void applyCoupon_returns200() {
        when(applyCouponUseCase.execute(any(), any())).thenReturn(Mono.empty());
        when(getCartWithProductsUseCase.execute(any())).thenReturn(Mono.just(buildCartDetail()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .post().uri("/api/cart/coupon")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"code":"PROMO10"}
                        """)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void removeCoupon_returns200() {
        when(removeCouponUseCase.execute(any())).thenReturn(Mono.empty());
        when(getCartWithProductsUseCase.execute(any())).thenReturn(Mono.just(buildCartDetail()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .delete().uri("/api/cart/coupon")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void selectShipping_returns200() {
        when(selectShippingOptionUseCase.execute(any(), any())).thenReturn(Mono.empty());
        when(getCartWithProductsUseCase.execute(any())).thenReturn(Mono.just(buildCartDetail()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .patch().uri("/api/cart/shipping")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"shippingOptionId":"standard"}
                        """)
                .exchange()
                .expectStatus().isOk();
    }
}
