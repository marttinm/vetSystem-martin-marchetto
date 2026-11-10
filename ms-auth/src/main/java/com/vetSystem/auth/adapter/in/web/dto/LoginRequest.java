package com.vetSystem.auth.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "El username es obligatorio") String username,
        @NotBlank(message = "La contrasenia es obligatoria") String password) {
}
