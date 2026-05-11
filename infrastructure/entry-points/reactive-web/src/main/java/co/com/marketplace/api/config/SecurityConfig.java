package co.com.marketplace.api.config;

import co.com.marketplace.model.gateway.TokenProviderGateway;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import reactor.core.publisher.Mono;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_ROUTES = {
            "/api/auth/login",
            "/api/auth/register/**",
            "/api/auth/refresh",
            "/api/auth/password-reset/**",
            "/api/catalog/**",
            "/actuator/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/webjars/**"
    };

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                          TokenProviderGateway tokenProvider) {
        AuthenticationWebFilter jwtFilter = jwtAuthenticationFilter(tokenProvider);

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(auth -> auth
                        .pathMatchers(HttpMethod.OPTIONS).permitAll()
                        .pathMatchers(PUBLIC_ROUTES).permitAll()
                        .pathMatchers("/api/admin/**").hasRole("ADMIN")
                        .pathMatchers("/api/producer/**").hasRole("PRODUCER")
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    private AuthenticationWebFilter jwtAuthenticationFilter(TokenProviderGateway tokenProvider) {
        AuthenticationWebFilter filter = new AuthenticationWebFilter(
                (ReactiveAuthenticationManager) Mono::just
        );
        filter.setServerAuthenticationConverter(jwtAuthenticationConverter(tokenProvider));
        return filter;
    }

    private ServerAuthenticationConverter jwtAuthenticationConverter(TokenProviderGateway tokenProvider) {
        return exchange -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Mono.empty();
            }
            String token = authHeader.substring(7);
            if (!tokenProvider.isTokenValid(token)) {
                return Mono.empty();
            }
            return tokenProvider.validateToken(token)
                    .map(userId -> {
                        String role = tokenProvider.extractRole(token);
                        var authorities = role != null
                                ? List.of(new SimpleGrantedAuthority("ROLE_" + role))
                                : List.<org.springframework.security.core.GrantedAuthority>of();
                        var auth = new UsernamePasswordAuthenticationToken(
                                userId.toString(), null, authorities
                        );
                        return (org.springframework.security.core.Authentication) auth;
                    });
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
