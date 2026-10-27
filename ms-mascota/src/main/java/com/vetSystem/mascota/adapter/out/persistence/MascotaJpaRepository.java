package com.vetSystem.mascota.adapter.out.persistence;

import com.vetSystem.mascota.domain.model.Mascota;
import com.vetSystem.mascota.domain.port.out.MascotaRepositoryPort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Adaptador de salida: JpaRepository ya implementa findAll, findById, save y deleteById
// del puerto, y Spring Data genera findByDuenioId a partir del nombre del metodo
@Repository
public interface MascotaJpaRepository extends JpaRepository<Mascota, Long>, MascotaRepositoryPort {

    @Override
    List<Mascota> findByDuenioId(Long duenioId);
}
