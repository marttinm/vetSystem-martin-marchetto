package com.vetSystem.Service;

import com.vetSystem.Dto.DuenioDTO;
import com.vetSystem.Entity.Duenio;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Mapper.DuenioMapper;
import com.vetSystem.Repository.DuenioRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DuenioService {

    private final DuenioRepository duenioRepository;
    private final DuenioMapper duenioMapper;

    @Transactional(readOnly = true)
    public List<DuenioDTO> getAllDuenios() {
        return duenioMapper.toDTOList(duenioRepository.findAll());
    }

    @Transactional(readOnly = true)
    public DuenioDTO getDuenioById(Long id) {
        return duenioMapper.toDTO(buscarOFallar(id));
    }

    @Transactional
    public DuenioDTO createDuenio(DuenioDTO dto) {
        if (duenioRepository.existsByDni(dto.getDni())) {
            throw new EntityExistsException("Ya existe un duenio con el DNI " + dto.getDni());
        }
        Duenio duenio = duenioMapper.toEntity(dto);
        duenio.setId(null);
        return duenioMapper.toDTO(duenioRepository.save(duenio));
    }

    @Transactional
    public DuenioDTO updateDuenio(Long id, DuenioDTO dto) {
        Duenio existente = buscarOFallar(id);

        existente.setNombre(dto.getNombre());
        existente.setApellido(dto.getApellido());
        existente.setTelefono(dto.getTelefono());
        existente.setEmail(dto.getEmail());

        return duenioMapper.toDTO(duenioRepository.save(existente));
    }

    @Transactional
    public void deleteDuenio(Long id) {
        duenioRepository.delete(buscarOFallar(id));
    }

    private Duenio buscarOFallar(Long id) {
        return duenioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Duenio", id));
    }
}
