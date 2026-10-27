package com.vetSystem.turno.application;

import com.vetSystem.turno.domain.exception.TurnoNotFoundException;
import com.vetSystem.turno.domain.exception.TurnoSuperpuestoException;
import com.vetSystem.turno.domain.model.EstadoTurno;
import com.vetSystem.turno.domain.model.Turno;
import com.vetSystem.turno.domain.port.in.TurnoService;
import com.vetSystem.turno.domain.port.out.TurnoRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TurnoServiceImpl implements TurnoService {

    private final TurnoRepositoryPort turnoRepository;

    public TurnoServiceImpl(TurnoRepositoryPort turnoRepository) {
        this.turnoRepository = turnoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Turno> findAll() {
        return turnoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Turno findById(Long id) {
        return turnoRepository.findById(id)
                .orElseThrow(() -> new TurnoNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Turno> findByVeterinarioId(Long veterinarioId) {
        return turnoRepository.findByVeterinarioId(veterinarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Turno> findByMascotaId(Long mascotaId) {
        return turnoRepository.findByMascotaId(mascotaId);
    }

    @Override
    @Transactional
    public Turno save(Turno turno) {
        turnoRepository.findFirstByVeterinarioIdAndFechaAndHora(
                        turno.getVeterinarioId(), turno.getFecha(), turno.getHora())
                .ifPresent(conflicto -> {
                    throw new TurnoSuperpuestoException("El veterinario ya tiene el turno con id "
                            + conflicto.getId() + " el " + conflicto.getFecha() + " a las " + conflicto.getHora());
                });
        turno.setId(null);
        turno.setEstado(EstadoTurno.PENDIENTE);
        return turnoRepository.save(turno);
    }

    @Override
    @Transactional
    public Turno cancelar(Long id) {
        Turno turno = findById(id);
        turno.setEstado(EstadoTurno.CANCELADO);
        return turnoRepository.save(turno);
    }
}
