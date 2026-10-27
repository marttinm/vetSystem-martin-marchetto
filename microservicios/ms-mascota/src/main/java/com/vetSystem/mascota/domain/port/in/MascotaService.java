package com.vetSystem.mascota.domain.port.in;

import com.vetSystem.mascota.domain.model.Mascota;

import java.util.List;

// Puerto de entrada: los casos de uso que el dominio ofrece sobre las mascotas
public interface MascotaService {

    List<Mascota> findAll();

    Mascota findById(Long id);

    List<Mascota> findByDuenioId(Long duenioId);

    Mascota save(Mascota mascota);

    Mascota update(Long id, Mascota mascota);

    void deleteById(Long id);
}
