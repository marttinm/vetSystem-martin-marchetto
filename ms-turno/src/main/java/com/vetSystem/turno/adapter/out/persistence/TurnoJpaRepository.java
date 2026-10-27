package com.vetSystem.turno.adapter.out.persistence;

import com.vetSystem.turno.domain.model.Turno;
import com.vetSystem.turno.domain.port.out.TurnoRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

// Adaptador de salida: JpaRepository ya implementa findAll, findById y save del puerto,
// y Spring Data genera las consultas restantes a partir del nombre de cada metodo
@Repository
public interface TurnoJpaRepository extends JpaRepository<Turno, Long>, TurnoRepositoryPort {

    @Override
    List<Turno> findByVeterinarioId(Long veterinarioId);

    @Override
    List<Turno> findByMascotaId(Long mascotaId);

    @Override
    Optional<Turno> findFirstByVeterinarioIdAndFechaAndHora(Long veterinarioId, LocalDate fecha, LocalTime hora);
}
