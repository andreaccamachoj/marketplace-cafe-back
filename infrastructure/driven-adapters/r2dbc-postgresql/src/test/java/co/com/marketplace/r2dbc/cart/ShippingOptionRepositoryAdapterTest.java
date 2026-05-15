package co.com.marketplace.r2dbc.cart;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShippingOptionRepositoryAdapterTest {

    @Mock private ShippingOptionReactiveRepository repo;

    @InjectMocks private ShippingOptionRepositoryAdapter adapter;

    private ShippingOptionData shippingData;

    @BeforeEach
    void setUp() {
        shippingData = ShippingOptionData.builder()
                .id("standard")
                .name("Envío estándar")
                .deliveryWindow("3-5 días")
                .price(BigDecimal.valueOf(8000))
                .isActive(true)
                .displayOrder(1)
                .build();
    }

    @Test
    void findAll_returnsOptions_whenFound() {
        when(repo.findAll()).thenReturn(Flux.just(shippingData));

        StepVerifier.create(adapter.findAll())
                .expectNextMatches(o -> "standard".equals(o.getId()) && "Envío estándar".equals(o.getName()))
                .verifyComplete();
    }

    @Test
    void findAll_returnsEmpty_whenNone() {
        when(repo.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(adapter.findAll())
                .verifyComplete();
    }

    @Test
    void findById_returnsOption_whenFound() {
        when(repo.findById("standard")).thenReturn(Mono.just(shippingData));

        StepVerifier.create(adapter.findById("standard"))
                .expectNextMatches(o -> "standard".equals(o.getId()))
                .verifyComplete();
    }

    @Test
    void findById_returnsEmpty_whenNotFound() {
        when(repo.findById("express")).thenReturn(Mono.empty());

        StepVerifier.create(adapter.findById("express"))
                .verifyComplete();
    }
}
