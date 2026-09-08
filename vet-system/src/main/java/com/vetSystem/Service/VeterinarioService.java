package com.vetSystem.Service;

import com.vetSystem.Dto.VeterinarioDTO;
import com.vetSystem.Entity.Veterinario;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Mapper.VeterinarioMapper;
import com.vetSystem.Repository.VeterinarioRepository;
import com.vetSystem.Exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VeterinarioService {

    private final VeterinarioRepository veterinarioRepository;
    private final VeterinarioMapper veterinarioMapper;

    @Transactional(readOnly = true)
    public List<VeterinarioDTO> getAllVeterinarios() {
        return veterinarioMapper.toDTOList(veterinarioRepository.findAll());
    }

    @Transactional(readOnly = true)
    public VeterinarioDTO getVeterinarioById(Long id) {
        return veterinarioMapper.toDTO(buscarOFallar(id));
    }

    @Transactional
    public VeterinarioDTO createVeterinario(VeterinarioDTO dto) {
        if (veterinarioRepository.existsByMatricula(dto.getMatricula())) {
            throw new DuplicateResourceException("Ya existe un veterinario con la matricula " + dto.getMatricula());
        }
        Veterinario veterinario = veterinarioMapper.toEntity(dto);
        veterinario.setId(null);
        return veterinarioMapper.toDTO(veterinarioRepository.save(veterinario));
    }

    @Transactional
    public VeterinarioDTO updateVeterinario(Long id, VeterinarioDTO dto) {
        Veterinario existente = buscarOFallar(id);

        existente.setNombre(dto.getNombre());
        existente.setApellido(dto.getApellido());
        existente.setEspecialidad(dto.getEspecialidad());

        return veterinarioMapper.toDTO(veterinarioRepository.save(existente));
    }

    @Transactional
    public void deleteVeterinario(Long id) {
        veterinarioRepository.delete(buscarOFallar(id));
    }

    private Veterinario buscarOFallar(Long id) {
        return veterinarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario", id));
    }
}
