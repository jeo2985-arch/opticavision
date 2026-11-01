package com.opticavision.service;

import com.opticavision.dao.UsuarioDAO;
import com.opticavision.model.Usuario;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioDAO usuarioDAO;

    public UsuarioDetailsService(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        if (correo == null || correo.isBlank()) {
            throw new UsernameNotFoundException("Credenciales no válidas");
        }
        Usuario usuario = usuarioDAO.buscarPorCorreo(correo.trim())
                .orElseThrow(() -> new UsernameNotFoundException("Credenciales no válidas"));
        String rol = usuario.getRol();
        if (!"CLIENTE".equals(rol) && !"VENDEDOR".equals(rol) && !"ADMIN".equals(rol)) {
            throw new UsernameNotFoundException("Credenciales no válidas");
        }
        return User.withUsername(usuario.getCorreo())
                .password(usuario.getPasswordHash())
                .roles(rol)
                .disabled(!usuario.isActivo())
                .build();
    }
}
