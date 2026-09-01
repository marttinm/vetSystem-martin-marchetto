package com.vetSystem.Repository;

import com.vetSystem.Entity.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {

    // Superposicion: el mismo veterinario, la misma fecha y la misma hora.
    boolean existsByVeterinarioIdAndFechaAndHora(Long veterinarioId, LocalDate fecha, LocalTime hora);

    // Agenda de un veterinario para un dia.
    List<Turno> findByVeterinarioIdAndFecha(Long veterinarioId, LocalDate fecha);

    // Historial de una mascota, del turno mas reciente al mas viejo.
    List<Turno> findByMascotaIdOrderByFechaDescHoraDesc(Long mascotaId);
}
