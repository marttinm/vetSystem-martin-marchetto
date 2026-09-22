package com.vetSystem.Service;

import com.vetSystem.Dto.MedicamentoResponseDTO;
import com.vetSystem.Dto.TurnoRequestDTO;
import com.vetSystem.Dto.TurnoResponseDTO;
import com.vetSystem.Entity.EstadoTurno;
import com.vetSystem.Entity.Mascota;
import com.vetSystem.Entity.Medicamento;
import com.vetSystem.Entity.Turno;
import com.vetSystem.Entity.Veterinario;
import com.vetSystem.Exception.BusinessRuleException;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Mapper.MedicamentoMapper;
import com.vetSystem.Mapper.TurnoMapper;
import com.vetSystem.Repository.MascotaRepository;
import com.vetSystem.Repository.MedicamentoRepository;
import com.vetSystem.Repository.TurnoRepository;
import com.vetSystem.Repository.VeterinarioRepository;
import com.vetSystem.Exception.TurnoSuperpuestoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final MascotaRepository mascotaRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final MedicamentoRepository medicamentoRepository;
    private final TurnoMapper turnoMapper;
    private final MedicamentoMapper medicamentoMapper;

    @Transactional(readOnly = true)
    public List<TurnoResponseDTO> getAllTurnos() {
        return turnoMapper.toDTOList(turnoRepository.findAll());
    }

    @Transactional(readOnly = true)
    public TurnoResponseDTO getTurnoById(Long id) {
        return turnoMapper.toDTO(buscarOFallar(id));
    }

    @Transactional(readOnly = true)
    public List<TurnoResponseDTO> getAgenda(Long veterinarioId, LocalDate fecha) {
        if (!veterinarioRepository.existsById(veterinarioId)) {
            throw new ResourceNotFoundException("Veterinario", veterinarioId);
        }
        return turnoMapper.toDTOList(turnoRepository.findByVeterinarioIdAndFecha(veterinarioId, fecha));
    }

    @Transactional(readOnly = true)
    public List<TurnoResponseDTO> getHistorialDeMascota(Long mascotaId) {
        if (!mascotaRepository.existsById(mascotaId)) {
            throw new ResourceNotFoundException("Mascota", mascotaId);
        }
        return turnoMapper.toDTOList(turnoRepository.findByMascotaIdOrderByFechaDescHoraDesc(mascotaId));
    }

    @Transactional
    public TurnoResponseDTO createTurno(TurnoRequestDTO request) {
        Mascota mascota = mascotaRepository.findById(request.getMascotaId())
                .orElseThrow(() -> new ResourceNotFoundException("Mascota", request.getMascotaId()));

        Veterinario veterinario = veterinarioRepository.findById(request.getVeterinarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario", request.getVeterinarioId()));

        if (turnoRepository.existsByVeterinarioIdAndFechaAndHora(
                request.getVeterinarioId(), request.getFecha(), request.getHora())) {
            throw new TurnoSuperpuestoException("El veterinario ya tiene un turno el "
                    + request.getFecha() + " a las " + request.getHora());
        }

        Turno turno = new Turno();
        turno.setFecha(request.getFecha());
        turno.setHora(request.getHora());
        turno.setMotivo(request.getMotivo());
        turno.setEstado(EstadoTurno.PENDIENTE);
        turno.setMascota(mascota);
        turno.setVeterinario(veterinario);

        return turnoMapper.toDTO(turnoRepository.save(turno));
    }

    @Transactional
    public TurnoResponseDTO actualizarEstado(Long id, EstadoTurno nuevoEstado, String observaciones) {
        Turno turno = buscarOFallar(id);
        turno.setEstado(nuevoEstado);
        if (observaciones != null) {
            turno.setObservaciones(observaciones);
        }
        return turnoMapper.toDTO(turnoRepository.save(turno));
    }

    @Transactional
    public void deleteTurno(Long id) {
        turnoRepository.delete(buscarOFallar(id));
    }

    @Transactional(readOnly = true)
    public List<MedicamentoResponseDTO> getMedicamentosDeTurno(Long turnoId) {
        return medicamentoMapper.toDTOList(buscarOFallar(turnoId).getMedicamentos());
    }

    @Transactional
    public List<MedicamentoResponseDTO> agregarMedicamento(Long turnoId, Long medicamentoId) {
        Turno turno = buscarOFallar(turnoId);
        Medicamento medicamento = medicamentoRepository.findById(medicamentoId)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento", medicamentoId));

        if (medicamento.getStock() <= 0) {
            throw new BusinessRuleException("El medicamento " + medicamento.getNombre()
                    + " (id " + medicamentoId + ") no tiene stock disponible");
        }

        medicamento.setStock(medicamento.getStock() - 1);
        turno.getMedicamentos().add(medicamento);
        turnoRepository.save(turno);

        return medicamentoMapper.toDTOList(turno.getMedicamentos());
    }

    private Turno buscarOFallar(Long id) {
        return turnoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turno", id));
    }
}
