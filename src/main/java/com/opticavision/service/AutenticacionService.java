package com.opticavision.service;

import com.opticavision.dao.UsuarioDAO;
import com.opticavision.model.Usuario;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AutenticacionService {
    private final UsuarioDAO usuarioDAO;
    private final SeguridadPassword seguridadPassword;
    public AutenticacionService(UsuarioDAO usuarioDAO, SeguridadPassword seguridadPassword) {
        this.usuarioDAO = usuarioDAO;
        this.seguridadPassword = seguridadPassword;
    }
    public Optional<Usuario> autenticar(String correo, String password) {
        if (correo == null || correo.isBlank() || password == null || password.isEmpty()) return Optional.empty();
        Optional<Usuario> encontrado = usuarioDAO.buscarPorCorreo(correo.trim());
        if (encontrado.isEmpty() || !encontrado.get().isActivo()) return Optional.empty();
        return seguridadPassword.verificarPassword(password, encontrado.get().getPasswordHash()) ? encontrado : Optional.empty();
    }
}
