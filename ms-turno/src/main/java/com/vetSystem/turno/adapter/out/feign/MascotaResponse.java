package com.vetSystem.turno.adapter.out.feign;

import lombok.Data;

// Copia reducida de la respuesta de ms-mascota: los microservicios no comparten clases de dominio
@Data
public class MascotaResponse {
    private Long id;
    private String nombre;
    private Long duenioId;
}
