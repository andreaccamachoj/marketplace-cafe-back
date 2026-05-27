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
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {CartRouter.class, CartHandler.class, GlobalErrorWebExceptionHandler.class, TestTxConfig.class})
class CartHandlerAddItemTest {

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
    void addItem_returns201() {
        when(addCartItemUseCase.execute(any(), any())).thenReturn(Mono.empty());
        when(getCartWithProductsUseCase.execute(any())).thenReturn(Mono.just(buildCartDetail()));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockUser(USER_ID))
                .post().uri("/api/cart/items")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"productId":"550e8400-e29b-41d4-a716-446655440002",
                         "quantity":2,"unitPriceSnapshot":25000}
                        """)
                .exchange()
                .expectStatus().is2xxSuccessful();
    }
}
