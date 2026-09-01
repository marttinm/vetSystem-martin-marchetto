package com.vetSystem.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DuenioDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String dni;
    private Integer telefono;
    private String email;
}
