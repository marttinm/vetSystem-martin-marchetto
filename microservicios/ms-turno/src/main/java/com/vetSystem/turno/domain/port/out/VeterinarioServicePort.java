package com.vetSystem.turno.domain.port.out;

// Puerto de salida hacia ms-veterinario: el dominio solo pregunta si existe, sin saber nada de HTTP
public interface VeterinarioServicePort {

    boolean existsById(Long id);
}
