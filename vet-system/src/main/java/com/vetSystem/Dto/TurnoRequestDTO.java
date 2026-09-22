package com.vetSystem.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "Datos necesarios para registrar un turno")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnoRequestDTO {

    @Schema(description = "Fecha del turno (hoy o futura)", example = "2026-10-15")
    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha del turno no puede ser en el pasado")
    private LocalDate fecha;

    @Schema(description = "Hora del turno", type = "string", example = "10:30")
    @NotNull(message = "La hora es obligatoria")
    private LocalTime hora;

    @Schema(description = "Motivo de la consulta", example = "Vacunacion antirrabica")
    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;

    @Schema(description = "ID de la mascota que se atiende", example = "1")
    @NotNull(message = "El id de la mascota es obligatorio")
    @Positive(message = "El id de la mascota debe ser positivo")
    private Long mascotaId;

    @Schema(description = "ID del veterinario que atiende", example = "1")
    @NotNull(message = "El id del veterinario es obligatorio")
    @Positive(message = "El id del veterinario debe ser positivo")
    private Long veterinarioId;
}
