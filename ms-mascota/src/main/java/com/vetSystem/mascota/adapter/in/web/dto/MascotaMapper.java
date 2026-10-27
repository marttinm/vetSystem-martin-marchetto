package com.vetSystem.mascota.adapter.in.web.dto;

import com.vetSystem.mascota.domain.model.Mascota;

public final class MascotaMapper {

    private MascotaMapper() {
    }

    public static Mascota toEntity(MascotaRequest request) {
        Mascota mascota = new Mascota();
        mascota.setNombre(request.getNombre());
        mascota.setEspecie(request.getEspecie());
        mascota.setRaza(request.getRaza());
        mascota.setFechaNacimiento(request.getFechaNacimiento());
        mascota.setDuenioId(request.getDuenioId());
        return mascota;
    }

    public static MascotaResponse toResponse(Mascota mascota) {
        return new MascotaResponse(
                mascota.getId(),
                mascota.getNombre(),
                mascota.getEspecie(),
                mascota.getRaza(),
                mascota.getFechaNacimiento(),
                mascota.getDuenioId());
    }
}
