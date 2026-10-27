package com.vetSystem.turno.domain.port.out;

import com.vetSystem.turno.domain.model.Turno;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

// Puerto de salida: lo que el dominio necesita de la persistencia, sin saber como se implementa
public interface TurnoRepositoryPort {

    List<Turno> findAll();

    Optional<Turno> findById(Long id);

    List<Turno> findByVeterinarioId(Long veterinarioId);

    List<Turno> findByMascotaId(Long mascotaId);

    Optional<Turno> findFirstByVeterinarioIdAndFechaAndHora(Long veterinarioId, LocalDate fecha, LocalTime hora);

    Turno save(Turno turno);
}
