package com.vetSystem.duenio.application;

import com.vetSystem.duenio.domain.exception.DuenioNotFoundException;
import com.vetSystem.duenio.domain.exception.DuplicatedEmailException;
import com.vetSystem.duenio.domain.model.Duenio;
import com.vetSystem.duenio.domain.port.in.DuenioService;
import com.vetSystem.duenio.domain.port.out.DuenioRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DuenioServiceImpl implements DuenioService {

    private final DuenioRepositoryPort duenioRepository;

    public DuenioServiceImpl(DuenioRepositoryPort duenioRepository) {
        this.duenioRepository = duenioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Duenio> findAll() {
        return duenioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Duenio findById(Long id) {
        return duenioRepository.findById(id)
                .orElseThrow(() -> new DuenioNotFoundException(id));
    }

    @Override
    @Transactional
    public Duenio save(Duenio duenio) {
        if (duenioRepository.existsByEmail(duenio.getEmail())) {
            throw new DuplicatedEmailException(duenio.getEmail());
        }
        duenio.setId(null);
        return duenioRepository.save(duenio);
    }

    @Override
    @Transactional
    public Duenio update(Long id, Duenio duenio) {
        Duenio existente = findById(id);
        existente.setNombre(duenio.getNombre());
        existente.setApellido(duenio.getApellido());
        existente.setTelefono(duenio.getTelefono());
        existente.setEmail(duenio.getEmail());
        return duenioRepository.save(existente);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id);
        duenioRepository.deleteById(id);
    }
}
