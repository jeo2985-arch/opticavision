package com.opticavision.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GestionUsuariosService {
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;

    public GestionUsuariosService(JdbcTemplate jdbc, PasswordEncoder encoder) {
        this.jdbc = jdbc;
        this.encoder = encoder;
    }

    private Map<String, Object> actor(String correo) {
        List<Map<String, Object>> filas = jdbc.queryForList("""
            SELECT id, rol, activo, puedeCrearUsuarios
            FROM dbo.Usuarios WHERE correo = ?
            """, correo);
        if (filas.isEmpty() || !Boolean.TRUE.equals(filas.get(0).get("activo"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cuenta no autorizada");
        }
        return filas.get(0);
    }

    private boolean esAdmin(Map<String, Object> usuario) {
        return "ADMIN".equals(usuario.get("rol"));
    }

    private boolean puedeCrear(Map<String, Object> usuario) {
        return esAdmin(usuario) || ("VENDEDOR".equals(usuario.get("rol"))
            && Boolean.TRUE.equals(usuario.get("puedeCrearUsuarios")));
    }

    private Map<String, Object> autorizado(String correo) {
        Map<String, Object> usuario = actor(correo);
        if (!puedeCrear(usuario)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Sin permiso para gestionar usuarios");
        }
        return usuario;
    }

    public Map<String, Object> contexto(String correo) {
        Map<String, Object> usuario = actor(correo);
        return Map.of("admin", esAdmin(usuario), "puedeCrearUsuarios", puedeCrear(usuario));
    }

    public List<Map<String, Object>> vendedores(String correo) {
        autorizado(correo);
        return jdbc.queryForList("""
            SELECT id, nombre, correo, rol, activo, puedeCrearUsuarios
            FROM dbo.Usuarios WHERE rol = 'VENDEDOR' ORDER BY id DESC
            """);
    }

    @Transactional
    public Map<String, Object> crear(Map<String, Object> datos, String correo) {
        Map<String, Object> actor = autorizado(correo);
        String nombre = texto(datos, "nombre", 100);
        String email = texto(datos, "correo", 150).toLowerCase(Locale.ROOT);
        String password = texto(datos, "password", 200);
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Correo no válido");
        }
        if (password.length() < 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener al menos 12 caracteres");
        }
        boolean permiso = Boolean.TRUE.equals(datos.get("puedeCrearUsuarios"));
        if (permiso && !esAdmin(actor)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo ADMIN puede otorgar permisos");
        }
        try {
            Integer id = jdbc.queryForObject("""
                INSERT INTO dbo.Usuarios(nombre, correo, passwordHash, rol, activo, puedeCrearUsuarios)
                OUTPUT INSERTED.id VALUES (?, ?, ?, 'VENDEDOR', 1, ?)
                """, Integer.class, nombre, email, encoder.encode(password), permiso);
            return Map.of("id", id, "mensaje", "Vendedor creado correctamente");
        } catch (DuplicateKeyException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está registrado");
        }
    }

    @Transactional
    public Map<String, Object> cambiarPermiso(int id, boolean permiso, String correo) {
        Map<String, Object> actor = actor(correo);
        if (!esAdmin(actor)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Solo ADMIN puede asignar permisos");
        }
        int filas = jdbc.update("""
            UPDATE dbo.Usuarios SET puedeCrearUsuarios = ?
            WHERE id = ? AND rol = 'VENDEDOR'
            """, permiso, id);
        if (filas != 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendedor no encontrado");
        }
        return Map.of("mensaje", "Permiso actualizado");
    }

    private String texto(Map<String, Object> datos, String campo, int maximo) {
        Object valor = datos == null ? null : datos.get(campo);
        if (!(valor instanceof String s) || s.isBlank() || s.trim().length() > maximo) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Dato inválido: " + campo);
        }
        return s.trim();
    }
}
