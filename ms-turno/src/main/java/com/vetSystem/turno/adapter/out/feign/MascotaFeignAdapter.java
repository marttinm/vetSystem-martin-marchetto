package com.vetSystem.turno.adapter.out.feign;

import com.vetSystem.turno.domain.port.out.MascotaServicePort;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MascotaFeignAdapter implements MascotaServicePort {

    private final MascotaFeignClient client;

    // El 404 de ms-mascota se traduce a false; cualquier otro error de Feign se propaga
    @Override
    public boolean existsById(Long id) {
        try {
            client.findById(id);
            return true;
        } catch (FeignException.NotFound e) {
            return false;
        }
    }
}
