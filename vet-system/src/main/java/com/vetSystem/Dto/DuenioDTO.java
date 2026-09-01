package com.vetSystem.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Lo que el cliente ve de un Duenio. Sin la lista de mascotas: se consulta
 *  por el endpoint anidado /api/duenios/{id}/mascotas. */
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
