package com.vetSystem.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "mascotas")
@NoArgsConstructor
@AllArgsConstructor
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

    // Muchas mascotas pertenecen a un dueño. Este es el lado dueño de la
    // relacion: la FK duenio_id vive en la tabla mascotas.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duenio_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Duenio duenio;
}
