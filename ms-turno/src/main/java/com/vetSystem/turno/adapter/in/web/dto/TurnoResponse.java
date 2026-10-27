package com.vetSystem.turno.adapter.in.web.dto;

import com.vetSystem.turno.domain.model.EstadoTurno;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnoResponse {
    private Long id;
    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;
    private EstadoTurno estado;
    private String observaciones;
    private Long mascotaId;
    private Long veterinarioId;
}
