package co.com.marketplace.r2dbc.catalog;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoastLevelRepositoryAdapterTest {

    @Mock private RoastLevelReactiveRepository repository;

    @InjectMocks private RoastLevelRepositoryAdapter adapter;

    private RoastLevelData roastData;

    @BeforeEach
    void setUp() {
        roastData = RoastLevelData.builder()
                .id(1)
                .code("LIGHT")
                .name("Ligero")
                .description("Tueste ligero")
                .icon("🌕")
                .build();
    }

    @Test
    void findAll_returnsRoastLevels_whenFound() {
        when(repository.findAll()).thenReturn(Flux.just(roastData));

        StepVerifier.create(adapter.findAll())
                .expectNextMatches(r -> "LIGHT".equals(r.getCode()) && "Ligero".equals(r.getName()))
                .verifyComplete();
    }

    @Test
    void findAll_returnsEmpty_whenNone() {
        when(repository.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(adapter.findAll())
                .verifyComplete();
    }

    @Test
    void findAll_propagatesError() {
        when(repository.findAll()).thenReturn(Flux.error(new RuntimeException("DB error")));

        StepVerifier.create(adapter.findAll())
                .verifyError(RuntimeException.class);
    }
}
