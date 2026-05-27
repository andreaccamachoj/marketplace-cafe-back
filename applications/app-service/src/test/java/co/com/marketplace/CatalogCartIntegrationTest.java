package co.com.marketplace;

import co.com.marketplace.api.cart.CartHandler;
import co.com.marketplace.api.cart.CartRouter;
import co.com.marketplace.api.catalog.CatalogHandler;
import co.com.marketplace.api.catalog.CatalogRouter;
import co.com.marketplace.api.config.GlobalErrorWebExceptionHandler;
import co.com.marketplace.api.config.SecurityConfig;
import co.com.marketplace.model.cart.Cart;
import co.com.marketplace.model.cart.CartItem;
import co.com.marketplace.model.cart.gateways.CartGateway;
import co.com.marketplace.model.catalog.Product;
import co.com.marketplace.model.catalog.gateways.ProductGateway;
import co.com.marketplace.model.gateway.TokenProviderGateway;
import co.com.marketplace.model.inventory.gateways.InventoryGateway;
import co.com.marketplace.usecase.cart.AddCartItemUseCase;
import co.com.marketplace.usecase.cart.ApplyCouponUseCase;
import co.com.marketplace.usecase.cart.ClearCartUseCase;
import co.com.marketplace.usecase.cart.GetCartUseCase;
import co.com.marketplace.usecase.cart.GetCartWithProductsUseCase;
import co.com.marketplace.usecase.cart.RemoveCartItemUseCase;
import co.com.marketplace.usecase.cart.RemoveCouponUseCase;
import co.com.marketplace.usecase.cart.SelectShippingOptionUseCase;
import co.com.marketplace.usecase.cart.UpdateCartItemQuantityUseCase;
import co.com.marketplace.usecase.catalog.GetCategoryBySlugUseCase;
import co.com.marketplace.usecase.catalog.GetFeaturedProductsUseCase;
import co.com.marketplace.usecase.catalog.GetProductByIdUseCase;
import co.com.marketplace.usecase.catalog.GetProductBySlugUseCase;
import co.com.marketplace.usecase.catalog.ListCategoriesUseCase;
import co.com.marketplace.usecase.catalog.ListCertificationsUseCase;
import co.com.marketplace.usecase.catalog.ListProductsUseCase;
import co.com.marketplace.usecase.catalog.ListRoastLevelsUseCase;
import co.com.marketplace.usecase.catalog.SearchProductsUseCase;
import co.com.marketplace.usecase.reviews.ListProductReviewsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {
        CatalogRouter.class, CatalogHandler.class,
        CartRouter.class, CartHandler.class,
        SecurityConfig.class,
        GlobalErrorWebExceptionHandler.class,
        CatalogCartIntegrationTest.RealUseCasesConfig.class
})
class CatalogCartIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean private ProductGateway productGateway;
    @MockitoBean private CartGateway cartGateway;
    @MockitoBean private InventoryGateway inventoryGateway;
    @MockitoBean private TokenProviderGateway tokenProviderGateway;
    @MockitoBean private TransactionalOperator tx;

    @MockitoBean private GetFeaturedProductsUseCase getFeaturedProductsUseCase;
    @MockitoBean private GetProductBySlugUseCase getProductBySlugUseCase;
    @MockitoBean private ListCategoriesUseCase listCategoriesUseCase;
    @MockitoBean private GetCategoryBySlugUseCase getCategoryBySlugUseCase;
    @MockitoBean private ListCertificationsUseCase listCertificationsUseCase;
    @MockitoBean private ListRoastLevelsUseCase listRoastLevelsUseCase;
    @MockitoBean private SearchProductsUseCase searchProductsUseCase;
    @MockitoBean private ListProductReviewsUseCase listProductReviewsUseCase;
    @MockitoBean private GetCartUseCase getCartUseCase;
    @MockitoBean private UpdateCartItemQuantityUseCase updateCartItemQuantityUseCase;
    @MockitoBean private ClearCartUseCase clearCartUseCase;
    @MockitoBean private ApplyCouponUseCase applyCouponUseCase;
    @MockitoBean private RemoveCouponUseCase removeCouponUseCase;
    @MockitoBean private SelectShippingOptionUseCase selectShippingOptionUseCase;

    @BeforeEach
    void setupTx() {
        lenient().when(tx.transactional(any(Mono.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @TestConfiguration
    static class RealUseCasesConfig {
        @Bean
        ListProductsUseCase listProductsUseCase(ProductGateway pg) {
            return new ListProductsUseCase(pg);
        }

        @Bean
        GetProductByIdUseCase getProductByIdUseCase(ProductGateway pg) {
            return new GetProductByIdUseCase(pg);
        }

        @Bean
        AddCartItemUseCase addCartItemUseCase(CartGateway cg) {
            return new AddCartItemUseCase(cg);
        }

        @Bean
        RemoveCartItemUseCase removeCartItemUseCase(CartGateway cg) {
            return new RemoveCartItemUseCase(cg);
        }

        @Bean
        GetCartWithProductsUseCase getCartWithProductsUseCase(CartGateway cg, ProductGateway pg) {
            return new GetCartWithProductsUseCase(cg, pg);
        }
    }

    @Test
    void listProducts_returns200WithList() {
        Product product = Product.builder()
                .id(UUID.randomUUID()).name("Café Test").price(BigDecimal.TEN).build();
        when(productGateway.findAll(any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt(), anyString()))
                .thenReturn(Flux.just(product));

        webTestClient.get().uri("/api/catalog/products")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].name").isEqualTo("Café Test");
    }

    @Test
    void getProductById_existing_returns200() {
        UUID id = UUID.randomUUID();
        Product product = Product.builder().id(id).name("Café Test").price(BigDecimal.TEN).build();
        when(productGateway.findById(id)).thenReturn(Mono.just(product));

        webTestClient.get().uri("/api/catalog/products/" + id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(id.toString());
    }

    @Test
    void getProductById_notFound_returns404() {
        UUID id = UUID.randomUUID();
        when(productGateway.findById(id)).thenReturn(Mono.empty());

        webTestClient.get().uri("/api/catalog/products/" + id)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void addCartItem_authenticated_returns201() {
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        String token = "cart-access-token";

        Cart cart = Cart.builder().id(cartId).userId(userId).items(List.of()).build();
        CartItem saved = CartItem.builder()
                .id(UUID.randomUUID()).cartId(cartId).productId(productId)
                .quantity(1).unitPriceSnapshot(BigDecimal.TEN).build();

        setupAuth(token, userId);
        when(cartGateway.findByUserId(userId)).thenReturn(Mono.just(cart));
        when(cartGateway.saveItem(any())).thenReturn(Mono.just(saved));

        webTestClient.post().uri("/api/cart/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("productId", productId, "quantity", 1, "unitPriceSnapshot", 10.0))
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void removeCartItem_authenticated_returns200() {
        UUID userId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        String token = "cart-access-token";

        Cart cart = Cart.builder().id(cartId).userId(userId).items(List.of()).build();

        setupAuth(token, userId);
        when(cartGateway.deleteItem(itemId)).thenReturn(Mono.empty());
        when(cartGateway.findByUserId(userId)).thenReturn(Mono.just(cart));

        webTestClient.delete().uri("/api/cart/items/" + itemId)
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isOk();
    }

    private void setupAuth(String token, UUID userId) {
        when(tokenProviderGateway.isTokenValid(token)).thenReturn(true);
        when(tokenProviderGateway.validateToken(token)).thenReturn(Mono.just(userId));
        when(tokenProviderGateway.extractRole(token)).thenReturn("BUYER");
    }
}
