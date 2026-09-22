package com.vetSystem.Service;

import com.vetSystem.Dto.MascotaDTO;
import com.vetSystem.Entity.Duenio;
import com.vetSystem.Entity.Mascota;
import com.vetSystem.Exception.BusinessRuleException;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Mapper.MascotaMapper;
import com.vetSystem.Repository.DuenioRepository;
import com.vetSystem.Repository.MascotaRepository;
import com.vetSystem.Exception.DuplicateResourceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MascotaService {

    private static final int MAX_MASCOTAS_POR_DUENIO = 5;

    private final MascotaRepository mascotaRepository;
    private final DuenioRepository duenioRepository;
    private final MascotaMapper mascotaMapper;

    @Transactional(readOnly = true)
    public List<MascotaDTO> getAllMascotas() {
        return mascotaMapper.toDTOList(mascotaRepository.findAll());
    }

    @Transactional(readOnly = true)
    public MascotaDTO getMascotaById(Long id) {
        return mascotaMapper.toDTO(buscarOFallar(id));
    }

    @Transactional(readOnly = true)
    public List<MascotaDTO> getMascotasByDueno(Long duenioId) {
        if (!duenioRepository.existsById(duenioId)) {
            throw new ResourceNotFoundException("Duenio", duenioId);
        }
        return mascotaMapper.toDTOList(mascotaRepository.findByDuenioId(duenioId));
    }

    @Transactional
    public MascotaDTO createMascota(Long duenioId, MascotaDTO dto) {
        Mascota mascota = mascotaMapper.toEntity(dto);
        Duenio duenio = duenioRepository.findById(duenioId)
                .orElseThrow(() -> new ResourceNotFoundException("Duenio", duenioId));

        if (mascotaRepository.countByDuenioId(duenioId) >= MAX_MASCOTAS_POR_DUENIO) {
            throw new BusinessRuleException("El duenio con id " + duenioId + " ya tiene "
                    + MAX_MASCOTAS_POR_DUENIO + " mascotas registradas, que es el maximo permitido");
        }

        if (mascotaRepository.existsByNombreAndDuenioId(mascota.getNombre(), duenioId)) {
            throw new DuplicateResourceException("Ya existe mascota con nombre " + mascota.getNombre());
        }

        mascota.setId(null);
        mascota.setDuenio(duenio);
        return mascotaMapper.toDTO(mascotaRepository.save(mascota));
    }

    @Transactional
    public MascotaDTO updateMascota(Long id, MascotaDTO datos) {
        Mascota existente = buscarOFallar(id);

        existente.setNombre(datos.getNombre());
        existente.setEspecie(datos.getEspecie());
        existente.setRaza(datos.getRaza());
        existente.setFechaNacimiento(datos.getFechaNacimiento());

        return mascotaMapper.toDTO(mascotaRepository.save(existente));
    }

    @Transactional
    public void deleteMascota(Long id) {
        mascotaRepository.delete(buscarOFallar(id));
    }

    private Mascota buscarOFallar(Long id) {
        return mascotaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mascota", id));
    }
}
