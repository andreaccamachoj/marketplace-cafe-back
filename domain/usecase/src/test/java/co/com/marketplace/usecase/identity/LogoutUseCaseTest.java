package co.com.marketplace.usecase.identity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class LogoutUseCaseTest {

    @InjectMocks
    private LogoutUseCase logoutUseCase;

    @Test
    void execute_completesWithoutError() {
        StepVerifier.create(logoutUseCase.execute(UUID.randomUUID()))
                .verifyComplete();
    }
}
