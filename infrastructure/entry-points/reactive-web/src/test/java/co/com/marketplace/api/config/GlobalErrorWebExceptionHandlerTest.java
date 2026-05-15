package co.com.marketplace.api.config;

import co.com.marketplace.model.exception.ConflictException;
import co.com.marketplace.model.exception.ForbiddenException;
import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.exception.UnauthorizedException;
import co.com.marketplace.model.exception.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalErrorWebExceptionHandlerTest {

    private GlobalErrorWebExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalErrorWebExceptionHandler(new ObjectMapper());
    }

    private MockServerWebExchange exchange() {
        return MockServerWebExchange.from(MockServerHttpRequest.get("/test/path").build());
    }

    @Test
    void notFoundException_returns404() {
        var ex = new MockServerWebExchange[]{exchange()};
        handler.handle(ex[0], new NotFoundException("NOT_FOUND", "not found")).block();
        assertEquals(HttpStatus.NOT_FOUND, ex[0].getResponse().getStatusCode());
    }

    @Test
    void conflictException_returns409() {
        var exch = exchange();
        handler.handle(exch, new ConflictException("CONFLICT", "conflict")).block();
        assertEquals(HttpStatus.CONFLICT, exch.getResponse().getStatusCode());
    }

    @Test
    void validationException_returns400() {
        var exch = exchange();
        handler.handle(exch, new ValidationException("VALIDATION", "invalid")).block();
        assertEquals(HttpStatus.BAD_REQUEST, exch.getResponse().getStatusCode());
    }

    @Test
    void unauthorizedException_returns401() {
        var exch = exchange();
        handler.handle(exch, new UnauthorizedException("UNAUTH", "unauthorized")).block();
        assertEquals(HttpStatus.UNAUTHORIZED, exch.getResponse().getStatusCode());
    }

    @Test
    void forbiddenException_returns403() {
        var exch = exchange();
        handler.handle(exch, new ForbiddenException("FORBIDDEN", "forbidden")).block();
        assertEquals(HttpStatus.FORBIDDEN, exch.getResponse().getStatusCode());
    }

    @Test
    void runtimeException_returns500() {
        var exch = exchange();
        handler.handle(exch, new RuntimeException("unexpected")).block();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exch.getResponse().getStatusCode());
    }
}
