package com.vetSystem.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

@Configuration
public class RateLimiterConfig {

    // Sin este bean el RequestRateLimiter usa el usuario autenticado como clave; como todavia
    // no hay autenticacion, la clave queda vacia y responde 403. Se limita por IP del cliente
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            InetSocketAddress remoteAddress = exchange.getRequest().getRemoteAddress();
            return Mono.just(remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "desconocido");
        };
    }
}
