package com.vetSystem.Dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Datos de un duenio de la clinica veterinaria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DuenioDTO {

    @Schema(description = "Identificador unico del duenio (lo genera la base de datos)", example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    @Schema(description = "Nombre del duenio", example = "Maria")
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    @Schema(description = "Apellido del duenio", example = "Gonzalez")
    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;
    @Schema(description = "DNI del duenio, de 7 u 8 digitos", example = "28543210")
    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 7, max = 8, message = "El DNI debe tener entre 7 y 8 digitos")
    private String dni;
    @Schema(description = "Telefono de contacto", example = "1145678901")
    private Integer telefono;
    @Schema(description = "Email de contacto", example = "maria.gonzalez@gmail.com")
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato valido")
    private String email;
}
