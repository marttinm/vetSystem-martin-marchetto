package com.vetSystem.auth.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "usuarios")
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String username;
    // Se guarda encriptada con BCrypt, nunca en texto plano
    @Column(nullable = false)
    private String password;
    // Ej: ADMIN, VET, RECEPCIONISTA
    @Column(nullable = false)
    private String rol;
}
