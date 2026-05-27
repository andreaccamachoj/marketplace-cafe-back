package co.com.marketplace.api.config;

import co.com.marketplace.model.exception.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalErrorWebExceptionHandlerDomainTest {

    private GlobalErrorWebExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalErrorWebExceptionHandler(new ObjectMapper());
    }

    private MockServerWebExchange exchange() {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/test/path").build());
    }

    static class CustomDomainException extends DomainException {
        CustomDomainException() {
            super("CUSTOM_DOMAIN_ERR", "Custom domain error");
        }
    }

    @Test
    void domainException_returns500() {
        var exch = exchange();
        handler.handle(exch, new CustomDomainException()).block();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exch.getResponse().getStatusCode());
    }
}
