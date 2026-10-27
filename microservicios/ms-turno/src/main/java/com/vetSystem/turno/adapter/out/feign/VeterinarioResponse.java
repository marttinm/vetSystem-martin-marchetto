package com.vetSystem.turno.adapter.out.feign;

import lombok.Data;

// Copia reducida de la respuesta de ms-veterinario: los microservicios no comparten clases de dominio
@Data
public class VeterinarioResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String especialidad;
}
