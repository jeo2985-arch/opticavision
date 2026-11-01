package com.opticavision.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class SeguridadPassword {
    private final PasswordEncoder encoder;
    public SeguridadPassword(PasswordEncoder encoder) { this.encoder = encoder; }
    public String encriptarPassword(String password) {
        if (password == null || password.isBlank()) throw new IllegalArgumentException("La contraseña no puede estar vacía");
        return encoder.encode(password);
    }
    public boolean verificarPassword(String password, String passwordHash) {
        if (password == null || password.isEmpty() || passwordHash == null || passwordHash.isBlank()) return false;
        return encoder.matches(password, passwordHash);
    }
}
