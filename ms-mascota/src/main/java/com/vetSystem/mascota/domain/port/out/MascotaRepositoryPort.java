package com.vetSystem.mascota.domain.port.out;

import com.vetSystem.mascota.domain.model.Mascota;

import java.util.List;
import java.util.Optional;

// Puerto de salida: lo que el dominio necesita de la persistencia, sin saber como se implementa
public interface MascotaRepositoryPort {

    List<Mascota> findAll();

    Optional<Mascota> findById(Long id);

    List<Mascota> findByDuenioId(Long duenioId);

    Mascota save(Mascota mascota);

    void deleteById(Long id);
}
