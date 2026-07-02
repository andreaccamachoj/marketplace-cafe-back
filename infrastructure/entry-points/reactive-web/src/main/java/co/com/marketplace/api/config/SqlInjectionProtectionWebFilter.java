package co.com.marketplace.api.config;

import co.com.marketplace.model.exception.ValidationException;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@Component
public class SqlInjectionProtectionWebFilter implements WebFilter {

    private static final String ERROR_CODE = "SQL_INJECTION_DETECTED";
    private static final String ERROR_MESSAGE_PREFIX = "Posible intento de inyección SQL detectado";

    private final ObjectMapper objectMapper;

    public SqlInjectionProtectionWebFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String path = request.getPath().value();
        if (SqlInjectionValidator.containsSqlInjection(path)) {
            return Mono.error(new ValidationException(
                    ERROR_CODE,
                    ERROR_MESSAGE_PREFIX + " en la ruta de la solicitud"));
        }

        for (Map.Entry<String, List<String>> entry : request.getQueryParams().entrySet()) {
            for (String value : entry.getValue()) {
                if (SqlInjectionValidator.containsSqlInjection(value)) {
                    return Mono.error(new ValidationException(
                            ERROR_CODE,
                            ERROR_MESSAGE_PREFIX + " en el parámetro '" + entry.getKey() + "'"));
                }
            }
        }

        if (!shouldInspectBody(request)) {
            return chain.filter(exchange);
        }

        DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();

        return DataBufferUtils.join(request.getBody())
                .map(buffer -> {
                    byte[] bytes = new byte[buffer.readableByteCount()];
                    buffer.read(bytes);
                    DataBufferUtils.release(buffer);
                    return bytes;
                })
                .defaultIfEmpty(new byte[0])
                .flatMap(bytes -> {
                    if (bytes.length > 0) {
                        try {
                            Object parsed = objectMapper.readValue(bytes, Object.class);
                            String offending = SqlInjectionValidator.findOffendingField(parsed, "");
                            if (offending != null) {
                                return Mono.<Void>error(new ValidationException(
                                        ERROR_CODE,
                                        ERROR_MESSAGE_PREFIX + " en el campo '" + offending + "'"));
                            }
                        } catch (Exception ignored) {
                            // Cuerpo no JSON parseable: dejar que el handler lo rechace con 400 estándar
                        }
                    }

                    ServerHttpRequest decorated = decorate(request, bytes, bufferFactory);
                    return chain.filter(exchange.mutate().request(decorated).build());
                });
    }

    private boolean shouldInspectBody(ServerHttpRequest request) {
        HttpMethod method = request.getMethod();
        if (!HttpMethod.POST.equals(method)
                && !HttpMethod.PUT.equals(method)
                && !HttpMethod.PATCH.equals(method)) {
            return false;
        }
        MediaType contentType = request.getHeaders().getContentType();
        return contentType != null && contentType.isCompatibleWith(MediaType.APPLICATION_JSON);
    }

    private ServerHttpRequest decorate(ServerHttpRequest request, byte[] bytes, DataBufferFactory factory) {
        return new ServerHttpRequestDecorator(request) {
            @Override
            public Flux<DataBuffer> getBody() {
                if (bytes.length == 0) {
                    return Flux.empty();
                }
                return Flux.just(factory.wrap(bytes));
            }
        };
    }
}
