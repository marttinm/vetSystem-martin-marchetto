package com.vetSystem.mascota.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "mascotas")
public class Mascota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nombre;
    @Column(nullable = false)
    private String especie;
    private String raza;
    private LocalDate fechaNacimiento;
    // Referencia por ID: el duenio vive en ms-duenio, con otra base de datos
    @Column(nullable = false)
    private Long duenioId;
}
