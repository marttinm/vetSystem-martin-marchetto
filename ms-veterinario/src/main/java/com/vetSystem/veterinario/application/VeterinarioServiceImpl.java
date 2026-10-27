package com.vetSystem.veterinario.application;

import com.vetSystem.veterinario.domain.exception.VeterinarioNotFoundException;
import com.vetSystem.veterinario.domain.model.Veterinario;
import com.vetSystem.veterinario.domain.port.in.VeterinarioService;
import com.vetSystem.veterinario.domain.port.out.VeterinarioRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VeterinarioServiceImpl implements VeterinarioService {

    private final VeterinarioRepositoryPort veterinarioRepository;

    public VeterinarioServiceImpl(VeterinarioRepositoryPort veterinarioRepository) {
        this.veterinarioRepository = veterinarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Veterinario> findAll() {
        return veterinarioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Veterinario findById(Long id) {
        return veterinarioRepository.findById(id)
                .orElseThrow(() -> new VeterinarioNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Veterinario> findByEspecialidad(String especialidad) {
        return veterinarioRepository.findByEspecialidad(especialidad);
    }

    @Override
    @Transactional
    public Veterinario save(Veterinario veterinario) {
        veterinario.setId(null);
        return veterinarioRepository.save(veterinario);
    }

    @Override
    @Transactional
    public Veterinario update(Long id, Veterinario veterinario) {
        Veterinario existente = findById(id);
        existente.setNombre(veterinario.getNombre());
        existente.setApellido(veterinario.getApellido());
        existente.setMatricula(veterinario.getMatricula());
        existente.setEspecialidad(veterinario.getEspecialidad());
        return veterinarioRepository.save(existente);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id);
        veterinarioRepository.deleteById(id);
    }
}
