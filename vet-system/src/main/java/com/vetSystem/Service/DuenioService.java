package com.vetSystem.Service;

import com.vetSystem.Entity.Duenio;
import com.vetSystem.Exception.ResourceNotFoundException;
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

    @Transactional(readOnly = true)
    public List<Duenio> getAllDuenios() {
        return duenioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Duenio getDuenioById(Long id) {
        return duenioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Duenio", id));
    }

    @Transactional
    public Duenio createDuenio(Duenio duenio) {
        if (duenioRepository.existsByDni(duenio.getDni())) {
            throw new EntityExistsException("Ya existe un duenio con el DNI " + duenio.getDni());
        }
        return duenioRepository.save(duenio);
    }

    @Transactional
    public Duenio updateDuenio(Long id, Duenio datos) {
        Duenio existente = this.getDuenioById(id);

        existente.setNombre(datos.getNombre());
        existente.setApellido(datos.getApellido());
        existente.setTelefono(datos.getTelefono());
        existente.setEmail(datos.getEmail());

        return duenioRepository.save(existente);
    }

    @Transactional
    public void deleteDuenio(Long id) {
        if (!duenioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Duenio", id);
        }
        duenioRepository.deleteById(id);
    }
}
