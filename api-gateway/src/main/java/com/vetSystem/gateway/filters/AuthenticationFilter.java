package com.vetSystem.gateway.filters;

import com.vetSystem.gateway.security.JwtService;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

// Valida el JWT de TODAS las requests antes de enrutarlas. Los microservicios de negocio
// no validan nada: confian en que el gateway ya rechazo las requests sin token valido
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;

    public AuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Login y registro son publicos: si no, no habria forma de conseguir el primer token
        if (path.startsWith("/auth/")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return rechazar(exchange);
        }

        String token = authHeader.substring(7);   // saca el "Bearer "
        if (!jwtService.validateToken(token)) {
            return rechazar(exchange);
        }

        // Token valido: le pasa el username al microservicio en el header X-Auth-User
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-Auth-User", jwtService.extractUsername(token))
                .build();
        return chain.filter(exchange.mutate().request(request).build());
    }

    private Mono<Void> rechazar(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    // Corre despues del LoggingFilter (-1), asi tambien se loguean las requests rechazadas con 401
    @Override
    public int getOrder() {
        return 0;
    }
}
