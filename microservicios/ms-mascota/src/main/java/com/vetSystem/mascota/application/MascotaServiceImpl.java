package com.vetSystem.mascota.application;

import com.vetSystem.mascota.domain.exception.MascotaNotFoundException;
import com.vetSystem.mascota.domain.model.Mascota;
import com.vetSystem.mascota.domain.port.in.MascotaService;
import com.vetSystem.mascota.domain.port.out.MascotaRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MascotaServiceImpl implements MascotaService {

    private final MascotaRepositoryPort mascotaRepository;

    public MascotaServiceImpl(MascotaRepositoryPort mascotaRepository) {
        this.mascotaRepository = mascotaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mascota> findAll() {
        return mascotaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Mascota findById(Long id) {
        return mascotaRepository.findById(id)
                .orElseThrow(() -> new MascotaNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Mascota> findByDuenioId(Long duenioId) {
        return mascotaRepository.findByDuenioId(duenioId);
    }

    @Override
    @Transactional
    public Mascota save(Mascota mascota) {
        mascota.setId(null);
        return mascotaRepository.save(mascota);
    }

    @Override
    @Transactional
    public Mascota update(Long id, Mascota mascota) {
        Mascota existente = findById(id);
        existente.setNombre(mascota.getNombre());
        existente.setEspecie(mascota.getEspecie());
        existente.setRaza(mascota.getRaza());
        existente.setFechaNacimiento(mascota.getFechaNacimiento());
        existente.setDuenioId(mascota.getDuenioId());
        return mascotaRepository.save(existente);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id);
        mascotaRepository.deleteById(id);
    }
}
