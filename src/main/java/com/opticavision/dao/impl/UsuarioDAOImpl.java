package com.opticavision.dao.impl;

import com.opticavision.dao.UsuarioDAO;
import com.opticavision.model.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.stereotype.Repository;

@Repository
public class UsuarioDAOImpl implements UsuarioDAO {
    private final DataSource dataSource;
    public UsuarioDAOImpl(DataSource dataSource) { this.dataSource = dataSource; }

    @Override
    public boolean registrar(Usuario usuario) {
        String sql = "INSERT INTO dbo.Usuarios (nombre, correo, passwordHash, rol, activo) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getCorreo());
            ps.setString(3, usuario.getPasswordHash());
            ps.setString(4, usuario.getRol());
            ps.setBoolean(5, usuario.isActivo());
            int filas = ps.executeUpdate();
            if (filas == 1) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) usuario.setId(rs.getInt(1));
                }
            }
            return filas == 1;
        } catch (SQLException e) { throw new IllegalStateException("Error al registrar usuario", e); }
    }

    @Override
    public Optional<Usuario> buscarPorId(int id) {
        String sql = "SELECT id,nombre,correo,passwordHash,rol,activo FROM dbo.Usuarios WHERE id=?";
        try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(convertir(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new IllegalStateException("Error al buscar usuario por ID", e); }
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        String sql = "SELECT id,nombre,correo,passwordHash,rol,activo FROM dbo.Usuarios WHERE correo=?";
        try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(convertir(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new IllegalStateException("Error al buscar usuario por correo", e); }
    }

    @Override
    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT id,nombre,correo,passwordHash,rol,activo FROM dbo.Usuarios ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(convertir(rs));
            return lista;
        } catch (SQLException e) { throw new IllegalStateException("Error al listar usuarios", e); }
    }

    @Override
    public boolean actualizar(Usuario usuario) {
        String sql = "UPDATE dbo.Usuarios SET nombre=?,correo=?,passwordHash=?,rol=?,activo=? WHERE id=?";
        try (Connection con = dataSource.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getCorreo());
            ps.setString(3, usuario.getPasswordHash());
            ps.setString(4, usuario.getRol());
            ps.setBoolean(5, usuario.isActivo());
            ps.setInt(6, usuario.getId());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) { throw new IllegalStateException("Error al actualizar usuario", e); }
    }

    private Usuario convertir(ResultSet rs) throws SQLException {
        return new Usuario(rs.getInt("id"), rs.getString("nombre"), rs.getString("correo"),
                rs.getString("passwordHash"), rs.getString("rol"), rs.getBoolean("activo"));
    }
}
