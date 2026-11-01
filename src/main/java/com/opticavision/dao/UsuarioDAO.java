package com.opticavision.dao;

import com.opticavision.model.Usuario;
import java.util.List;
import java.util.Optional;

public interface UsuarioDAO {
    boolean registrar(Usuario usuario);
    Optional<Usuario> buscarPorId(int id);
    Optional<Usuario> buscarPorCorreo(String correo);
    List<Usuario> listarTodos();
    boolean actualizar(Usuario usuario);
}
