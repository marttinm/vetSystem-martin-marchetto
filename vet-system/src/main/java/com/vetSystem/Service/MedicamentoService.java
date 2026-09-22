package com.vetSystem.Service;

import com.vetSystem.Dto.MedicamentoRequestDTO;
import com.vetSystem.Dto.MedicamentoResponseDTO;
import com.vetSystem.Entity.Medicamento;
import com.vetSystem.Exception.BusinessRuleException;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Mapper.MedicamentoMapper;
import com.vetSystem.Repository.MedicamentoRepository;
import com.vetSystem.Repository.TurnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicamentoService {

    private final MedicamentoRepository medicamentoRepository;
    private final TurnoRepository turnoRepository;
    private final MedicamentoMapper medicamentoMapper;

    @Transactional(readOnly = true)
    public List<MedicamentoResponseDTO> getAllMedicamentos() {
        return medicamentoMapper.toDTOList(medicamentoRepository.findAll());
    }

    @Transactional(readOnly = true)
    public MedicamentoResponseDTO getMedicamentoById(Long id) {
        return medicamentoMapper.toDTO(buscarOFallar(id));
    }

    @Transactional
    public MedicamentoResponseDTO createMedicamento(MedicamentoRequestDTO dto) {
        Medicamento medicamento = medicamentoMapper.toEntity(dto);
        return medicamentoMapper.toDTO(medicamentoRepository.save(medicamento));
    }

    @Transactional
    public MedicamentoResponseDTO updateMedicamento(Long id, MedicamentoRequestDTO dto) {
        Medicamento existente = buscarOFallar(id);

        existente.setNombre(dto.getNombre());
        existente.setPrincipioActivo(dto.getPrincipioActivo());
        existente.setStock(dto.getStock());
        existente.setPrecioUnitario(dto.getPrecioUnitario());

        return medicamentoMapper.toDTO(medicamentoRepository.save(existente));
    }

    @Transactional
    public void deleteMedicamento(Long id) {
        Medicamento medicamento = buscarOFallar(id);
        if (turnoRepository.existsByMedicamentosId(id)) {
            throw new BusinessRuleException("El medicamento con id " + id
                    + " no se puede eliminar porque esta recetado en al menos un turno");
        }
        medicamentoRepository.delete(medicamento);
    }

    private Medicamento buscarOFallar(Long id) {
        return medicamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medicamento", id));
    }
}
