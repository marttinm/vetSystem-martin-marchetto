package com.vetSystem.turno.domain.port.out;

// Puerto de salida hacia ms-mascota: el dominio solo pregunta si existe, sin saber nada de HTTP
public interface MascotaServicePort {

    boolean existsById(Long id);
}
