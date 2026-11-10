package com.vetSystem.auth.adapter.in.web;

import com.vetSystem.auth.adapter.in.web.dto.AuthResponse;
import com.vetSystem.auth.adapter.in.web.dto.LoginRequest;
import com.vetSystem.auth.adapter.in.web.dto.RegisterRequest;
import com.vetSystem.auth.domain.model.Usuario;
import com.vetSystem.auth.domain.port.in.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        Usuario usuario = new Usuario(null, request.username(), request.password(), request.rol());
        return ResponseEntity.ok(new AuthResponse(authService.register(usuario)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(new AuthResponse(authService.login(request.username(), request.password())));
    }
}
