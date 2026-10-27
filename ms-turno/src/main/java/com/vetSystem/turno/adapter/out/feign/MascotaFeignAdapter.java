package com.vetSystem.turno.adapter.out.feign;

import com.vetSystem.turno.domain.exception.ServicioNoDisponibleException;
import com.vetSystem.turno.domain.port.out.MascotaServicePort;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MascotaFeignAdapter implements MascotaServicePort {

    private final MascotaFeignClient client;

    // El 404 de ms-mascota se traduce a false y no cuenta como fallo del circuito;
    // cualquier otro error de Feign se reintenta y lo registra el Circuit Breaker
    @Override
    @CircuitBreaker(name = "mascotaService", fallbackMethod = "fallbackMascota")
    @Retry(name = "mascotaService", fallbackMethod = "fallbackMascota")
    public boolean existsById(Long id) {
        try {
            client.findById(id);
            return true;
        } catch (FeignException.NotFound e) {
            return false;
        }
    }

    // Lo llaman el Retry (reintentos agotados) y el Circuit Breaker (circuito abierto o fallo);
    // si el Retry ya armo la excepcion, se propaga sin volver a envolver el mensaje
    private boolean fallbackMascota(Long mascotaId, Throwable ex) {
        if (ex instanceof ServicioNoDisponibleException servicioNoDisponible) {
            throw servicioNoDisponible;
        }
        throw new ServicioNoDisponibleException(
                "El servicio de mascotas no esta disponible: " + ex.getMessage());
    }
}
