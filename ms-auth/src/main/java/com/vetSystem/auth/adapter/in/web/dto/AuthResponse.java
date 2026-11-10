package com.vetSystem.auth.adapter.in.web.dto;

// Lo que devuelven /auth/register y /auth/login
public record AuthResponse(String token) {
}
