package com.vetSystem.duenio.adapter.in.web.dto;

import com.vetSystem.duenio.domain.model.Duenio;

public final class DuenioMapper {

    private DuenioMapper() {
    }

    public static Duenio toEntity(DuenioRequest request) {
        Duenio duenio = new Duenio();
        duenio.setNombre(request.getNombre());
        duenio.setApellido(request.getApellido());
        duenio.setDni(request.getDni());
        duenio.setTelefono(request.getTelefono());
        duenio.setEmail(request.getEmail());
        return duenio;
    }

    public static DuenioResponse toResponse(Duenio duenio) {
        return new DuenioResponse(
                duenio.getId(),
                duenio.getNombre(),
                duenio.getApellido(),
                duenio.getDni(),
                duenio.getTelefono(),
                duenio.getEmail());
    }
}
