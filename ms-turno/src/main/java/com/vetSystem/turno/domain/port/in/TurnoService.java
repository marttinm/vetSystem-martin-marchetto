package com.vetSystem.turno.domain.port.in;

import com.vetSystem.turno.domain.model.Turno;

import java.util.List;

// Puerto de entrada: los casos de uso que el dominio ofrece sobre los turnos
public interface TurnoService {

    List<Turno> findAll();

    Turno findById(Long id);

    List<Turno> findByVeterinarioId(Long veterinarioId);

    List<Turno> findByMascotaId(Long mascotaId);

    Turno save(Turno turno);

    Turno cancelar(Long id);
}
