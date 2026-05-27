package co.com.marketplace.api.config;

import co.com.marketplace.api.shared.ApiError;
import co.com.marketplace.model.exception.ConflictException;
import co.com.marketplace.model.exception.DomainException;
import co.com.marketplace.model.exception.ForbiddenException;
import co.com.marketplace.model.exception.NotFoundException;
import co.com.marketplace.model.exception.UnauthorizedException;
import co.com.marketplace.model.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

@Component
@Order(-2)
public class GlobalErrorWebExceptionHandler implements WebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalErrorWebExceptionHandler.class);

    private final ObjectMapper objectMapper;

    public GlobalErrorWebExceptionHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        var response = exchange.getResponse();
        var path = exchange.getRequest().getPath().value();

        HttpStatus status;
        ApiError apiError;

        if (ex instanceof NotFoundException e) {
            status = HttpStatus.NOT_FOUND;
            apiError = ApiError.of(e.getCode(), e.getMessage(), path);
        } else if (ex instanceof ConflictException e) {
            status = HttpStatus.CONFLICT;
            apiError = ApiError.of(e.getCode(), e.getMessage(), path);
        } else if (ex instanceof ValidationException e) {
            status = HttpStatus.BAD_REQUEST;
            apiError = ApiError.of(e.getCode(), e.getMessage(), path);
        } else if (ex instanceof UnauthorizedException e) {
            status = HttpStatus.UNAUTHORIZED;
            apiError = ApiError.of(e.getCode(), e.getMessage(), path);
        } else if (ex instanceof ForbiddenException e) {
            status = HttpStatus.FORBIDDEN;
            apiError = ApiError.of(e.getCode(), e.getMessage(), path);
        } else if (ex instanceof DomainException e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            apiError = ApiError.of(e.getCode(), e.getMessage(), path);
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            apiError = ApiError.of("INTERNAL_ERROR", "An unexpected error occurred", path);
            log.error("Unhandled exception on {} {}", exchange.getRequest().getMethod(), path, ex);
        }

        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        DataBufferFactory bufferFactory = response.bufferFactory();
        byte[] body = objectMapper.writeValueAsBytes(apiError);
        return response.writeWith(Mono.just(bufferFactory.wrap(body)));
    }
}
