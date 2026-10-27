package com.vetSystem.veterinario.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VeterinarioRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;
    @NotBlank(message = "La matricula es obligatoria")
    @Pattern(regexp = "MV-\\d+", message = "La matricula debe tener el formato MV-1234")
    private String matricula;
    @NotBlank(message = "La especialidad es obligatoria")
    private String especialidad;
}
