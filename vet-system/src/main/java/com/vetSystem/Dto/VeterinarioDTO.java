package com.vetSystem.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Datos de un veterinario de la clinica")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VeterinarioDTO {

    @Schema(description = "Identificador unico del veterinario (lo genera la base de datos)", example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nombre del veterinario", example = "Lucia")
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Schema(description = "Apellido del veterinario", example = "Fernandez")
    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @Schema(description = "Matricula profesional con formato MV-numero", example = "MV-4521")
    @NotBlank(message = "La matricula es obligatoria")
    @Pattern(regexp = "MV-\\d+", message = "La matricula debe tener el formato MV-1234")
    private String matricula;

    @Schema(description = "Especialidad del veterinario", example = "Cirugia")
    @NotBlank(message = "La especialidad es obligatoria")
    private String especialidad;
}
