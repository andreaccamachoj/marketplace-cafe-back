package co.com.marketplace.r2dbc.cart;

import co.com.marketplace.model.cart.Cart;
import co.com.marketplace.model.cart.CartItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.UUID;
import java.util.function.BiFunction;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"unchecked", "rawtypes"})
@ExtendWith(MockitoExtension.class)
class CartRepositoryAdapterTest {

    @Mock private CartReactiveRepository cartRepo;
    @Mock private CartItemReactiveRepository cartItemRepo;
    @Mock private R2dbcEntityTemplate template;
    @Mock private DatabaseClient db;
    @Mock private DatabaseClient.GenericExecuteSpec spec;
    @Mock private RowsFetchSpec fetchSpec;

    @InjectMocks private CartRepositoryAdapter adapter;

    private final UUID cartId = UUID.randomUUID();
    private final UUID userId = UUID.randomUUID();
    private final UUID itemId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private CartData cartData;
    private Cart cart;

    @BeforeEach
    void setUp() {
        cartData = CartData.builder()
                .id(cartId)
                .userId(userId)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();

        cart = Cart.builder()
                .id(cartId)
                .userId(userId)
                .items(Collections.emptyList())
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    @Test
    void findByUserId_returnsCart_whenFound() {
        when(cartRepo.findByUserId(userId)).thenReturn(Mono.just(cartData));
        when(cartItemRepo.findByCartId(cartId)).thenReturn(Flux.empty());

        StepVerifier.create(adapter.findByUserId(userId))
                .expectNextMatches(c -> cartId.equals(c.getId()) && userId.equals(c.getUserId()))
                .verifyComplete();
    }

    @Test
    void findByUserId_returnsEmpty_whenNotFound() {
        when(cartRepo.findByUserId(userId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findByUserId(userId))
                .verifyComplete();
    }

    @Test
    void save_returnsCart_whenSuccessful() {
        when(cartRepo.save(any(CartData.class))).thenReturn(Mono.just(cartData));

        StepVerifier.create(adapter.save(cart))
                .expectNextMatches(c -> cartId.equals(c.getId()))
                .verifyComplete();
    }

    @Test
    void deleteItem_completesSuccessfully() {
        when(cartItemRepo.deleteById(itemId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.deleteItem(itemId))
                .verifyComplete();
    }

    @Test
    void clearItems_completesSuccessfully() {
        when(cartItemRepo.deleteByCartId(cartId)).thenReturn(Mono.empty());

        StepVerifier.create(adapter.clearItems(cartId))
                .verifyComplete();
    }

    @Test
    void applyCoupon_completesSuccessfully() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        when(spec.then()).thenReturn(Mono.empty());

        StepVerifier.create(adapter.applyCoupon(cartId, 1))
                .verifyComplete();
    }

    @Test
    void removeCoupon_completesSuccessfully() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        when(spec.then()).thenReturn(Mono.empty());

        StepVerifier.create(adapter.removeCoupon(cartId))
                .verifyComplete();
    }

    @Test
    void applyShipping_completesSuccessfully() {
        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        when(spec.then()).thenReturn(Mono.empty());

        StepVerifier.create(adapter.applyShipping(cartId, "standard"))
                .verifyComplete();
    }

    @Test
    void saveItem_returnsItem_whenSuccessful() {
        CartItemData itemData = CartItemData.builder()
                .id(itemId)
                .cartId(cartId)
                .productId(productId)
                .quantity(2)
                .unitPriceSnapshot(BigDecimal.valueOf(25000))
                .addedAt(OffsetDateTime.now())
                .build();

        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(itemData)).when(fetchSpec).one();

        CartItem item = CartItem.builder()
                .id(itemId)
                .cartId(cartId)
                .productId(productId)
                .quantity(2)
                .unitPriceSnapshot(BigDecimal.valueOf(25000))
                .addedAt(OffsetDateTime.now())
                .build();

        StepVerifier.create(adapter.saveItem(item))
                .expectNextMatches(i -> cartId.equals(i.getCartId()) && 2 == i.getQuantity())
                .verifyComplete();
    }

    @Test
    void updateItemQuantity_returnsItem_whenSuccessful() {
        CartItemData itemData = CartItemData.builder()
                .id(itemId)
                .cartId(cartId)
                .productId(productId)
                .quantity(5)
                .unitPriceSnapshot(BigDecimal.valueOf(25000))
                .addedAt(OffsetDateTime.now())
                .build();

        when(db.sql(anyString())).thenReturn(spec);
        when(spec.bind(anyString(), any())).thenReturn(spec);
        doReturn(fetchSpec).when(spec).map(any(BiFunction.class));
        doReturn(Mono.just(itemData)).when(fetchSpec).one();

        StepVerifier.create(adapter.updateItemQuantity(itemId, 5))
                .expectNextMatches(i -> 5 == i.getQuantity())
                .verifyComplete();
    }
}
