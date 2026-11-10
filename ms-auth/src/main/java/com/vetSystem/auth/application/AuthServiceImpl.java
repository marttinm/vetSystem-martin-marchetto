package com.vetSystem.auth.application;

import com.vetSystem.auth.domain.exception.CredencialesInvalidasException;
import com.vetSystem.auth.domain.exception.UsuarioYaExisteException;
import com.vetSystem.auth.domain.model.Usuario;
import com.vetSystem.auth.domain.port.in.AuthService;
import com.vetSystem.auth.domain.port.out.UsuarioRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepositoryPort usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UsuarioRepositoryPort usuarioRepository, PasswordEncoder passwordEncoder,
                           JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    @Transactional
    public String register(Usuario usuario) {
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            throw new UsuarioYaExisteException(usuario.getUsername());
        }
        usuario.setId(null);
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuarioRepository.save(usuario);
        return jwtService.generateToken(usuario.getUsername(), usuario.getRol());
    }

    @Override
    @Transactional(readOnly = true)
    public String login(String username, String password) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(CredencialesInvalidasException::new);
        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new CredencialesInvalidasException();
        }
        return jwtService.generateToken(usuario.getUsername(), usuario.getRol());
    }
}
