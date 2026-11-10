package com.vetSystem.auth.domain.port.in;

import com.vetSystem.auth.domain.model.Usuario;

// Puerto de entrada: los dos casos de uso de ms-auth. Los dos devuelven el token JWT
public interface AuthService {

    String register(Usuario usuario);

    String login(String username, String password);
}
