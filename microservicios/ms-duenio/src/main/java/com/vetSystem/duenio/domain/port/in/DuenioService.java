package com.vetSystem.duenio.domain.port.in;

import com.vetSystem.duenio.domain.model.Duenio;

import java.util.List;

// Puerto de entrada: los casos de uso que el dominio ofrece sobre los duenios
public interface DuenioService {

    List<Duenio> findAll();

    Duenio findById(Long id);

    Duenio save(Duenio duenio);

    Duenio update(Long id, Duenio duenio);

    void deleteById(Long id);
}
