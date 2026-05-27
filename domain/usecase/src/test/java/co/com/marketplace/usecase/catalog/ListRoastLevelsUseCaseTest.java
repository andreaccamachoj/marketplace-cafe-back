package co.com.marketplace.usecase.catalog;

import co.com.marketplace.model.catalog.RoastLevel;
import co.com.marketplace.model.catalog.gateways.RoastLevelGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListRoastLevelsUseCaseTest {

    @Mock private RoastLevelGateway roastLevelGateway;

    @InjectMocks
    private ListRoastLevelsUseCase useCase;

    @Test
    void execute_returnsRoastLevels() {
        RoastLevel rl = RoastLevel.builder().id(1).code("LIGHT").name("Light Roast").build();
        when(roastLevelGateway.findAll()).thenReturn(Flux.just(rl));

        StepVerifier.create(useCase.execute())
                .expectNextMatches(r -> "LIGHT".equals(r.getCode()))
                .verifyComplete();
    }

    @Test
    void execute_returnsEmpty_whenNone() {
        when(roastLevelGateway.findAll()).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute())
                .verifyComplete();
    }
}
