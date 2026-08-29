package com.vetSystem.Service;

import com.vetSystem.Entity.Duenio;
import com.vetSystem.Entity.Mascota;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Repository.DuenioRepository;
import com.vetSystem.Repository.MascotaRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MascotaService {

    private final MascotaRepository mascotaRepository;
    private final DuenioRepository duenioRepository;

    @Transactional(readOnly = true)
    public List<Mascota> getAllMascotas() {
        return mascotaRepository.findAll();
    }

    @Transactional
    public Mascota getMascotaById(Long id){
        return mascotaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mascota", id));
    }

    @Transactional
    public List<Mascota> getMascotasByDueno(Long duenioId){
        if (!duenioRepository.existsById(duenioId)){
            throw new ResourceNotFoundException("Duenio", duenioId);
        }
        return mascotaRepository.findByDuenioId(duenioId);
    }

    @Transactional
    public Mascota createMascota(Long duenioId, Mascota mascota){
        Duenio duenio = duenioRepository.findById(duenioId)
                .orElseThrow(() -> new ResourceNotFoundException("Duenio", duenioId));

        if (mascotaRepository.existsByNombreAndDuenioId(mascota.getNombre(), duenioId)){
            throw new EntityExistsException("Ya existe mascota con nombre " + mascota.getNombre());
        }
        mascota.setDuenio(duenio);
        return mascotaRepository.save(mascota);
    }

    @Transactional
    public Mascota updateMascota(Long id, Mascota mascota) {
        Mascota existente = this.getMascotaById(id);

        existente.setNombre(mascota.getNombre());
        existente.setEspecie(mascota.getEspecie());
        existente.setRaza(mascota.getRaza());
        existente.setFechaNacimiento(mascota.getFechaNacimiento());

        return mascotaRepository.save(existente);
    }

    @Transactional
    public void deleteMascota(Long id){
        if (!mascotaRepository.existsById(id)){
            throw new ResourceNotFoundException("Mascota", id);
        }
        mascotaRepository.deleteById(id);
    }

}
