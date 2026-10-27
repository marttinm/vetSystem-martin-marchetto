package com.vetSystem.turno.adapter.in.web.dto;

import com.vetSystem.turno.domain.model.Turno;

public final class TurnoMapper {

    private TurnoMapper() {
    }

    // El estado no viene en el request: todo turno nuevo lo arranca el dominio como PENDIENTE
    public static Turno toEntity(TurnoRequest request) {
        Turno turno = new Turno();
        turno.setFecha(request.getFecha());
        turno.setHora(request.getHora());
        turno.setMotivo(request.getMotivo());
        turno.setMascotaId(request.getMascotaId());
        turno.setVeterinarioId(request.getVeterinarioId());
        return turno;
    }

    public static TurnoResponse toResponse(Turno turno) {
        return new TurnoResponse(
                turno.getId(),
                turno.getFecha(),
                turno.getHora(),
                turno.getMotivo(),
                turno.getEstado(),
                turno.getObservaciones(),
                turno.getMascotaId(),
                turno.getVeterinarioId());
    }
}
