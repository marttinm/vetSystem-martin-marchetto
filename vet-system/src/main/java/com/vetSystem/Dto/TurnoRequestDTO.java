package com.vetSystem.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/** Lo que el cliente MANDA para pedir un turno. No trae id ni estado:
 *  el id lo genera la base y el estado arranca siempre en PENDIENTE. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnoRequestDTO {
    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;
    private Long mascotaId;
    private Long veterinarioId;
}
