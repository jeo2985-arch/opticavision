
package com.opticavision.config;

import com.opticavision.dao.UsuarioDAO;
import com.opticavision.model.Usuario;
import com.opticavision.service.AutenticacionService;
import com.opticavision.service.SeguridadPassword;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import javax.sql.DataSource;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PruebaAutenticacion implements CommandLineRunner {

    private final UsuarioDAO usuarioDAO;
    private final AutenticacionService autenticacion;
    private final SeguridadPassword seguridadPassword;
    private final DataSource dataSource;

    public PruebaAutenticacion(
            UsuarioDAO usuarioDAO,
            AutenticacionService autenticacion,
            SeguridadPassword seguridadPassword,
            DataSource dataSource) {

        this.usuarioDAO = usuarioDAO;
        this.autenticacion = autenticacion;
        this.seguridadPassword = seguridadPassword;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {

        String correo = "test_" + UUID.randomUUID()
                + "@example.com";

        String password = "Temporal#2026";
        String nombre = "PRUEBA_AUTENTICACION_TEMPORAL";

        int pruebasCorrectas = 0;
        boolean registrado = false;

        System.out.println();
        System.out.println("==============================");
        System.out.println("PRUEBAS DE AUTENTICACION");
        System.out.println("==============================");

        try {

            // Crear un usuario temporal con BCrypt
            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setCorreo(correo);
            usuario.setPasswordHash(
                    seguridadPassword.encriptarPassword(password)
            );
            usuario.setRol("CLIENTE");
            usuario.setActivo(true);

            registrado = usuarioDAO.registrar(usuario);

            if (!registrado) {
                throw new IllegalStateException(
                        "No se pudo registrar el usuario temporal"
                );
            }

            // PRUEBA 1: CREDENCIALES CORRECTAS
            Optional<Usuario> acceso =
                    autenticacion.autenticar(correo, password);

            if (acceso.isPresent()
                    && "CLIENTE".equals(acceso.get().getRol())) {

                System.out.println(
                        "[OK] 1. Credenciales correctas");
                pruebasCorrectas++;
            } else {
                System.out.println(
                        "[ERROR] 1. Credenciales correctas");
            }

            // PRUEBA 2: CONTRASEÑA INCORRECTA
            if (autenticacion.autenticar(
                    correo, "Incorrecta#2026").isEmpty()) {

                System.out.println(
                        "[OK] 2. Contraseña incorrecta rechazada");
                pruebasCorrectas++;
            } else {
                System.out.println(
                        "[ERROR] 2. Contraseña incorrecta");
            }

            // PRUEBA 3: CORREO INEXISTENTE
            if (autenticacion.autenticar(
                    "noexiste_" + UUID.randomUUID()
                    + "@example.com", password).isEmpty()) {

                System.out.println(
                        "[OK] 3. Correo inexistente rechazado");
                pruebasCorrectas++;
            } else {
                System.out.println(
                        "[ERROR] 3. Correo inexistente");
            }

            // PRUEBA 4: CUENTA INACTIVA
            usuario.setActivo(false);

            if (!usuarioDAO.actualizar(usuario)) {
                throw new IllegalStateException(
                        "No se pudo desactivar la cuenta temporal"
                );
            }

            if (autenticacion.autenticar(
                    correo, password).isEmpty()) {

                System.out.println(
                        "[OK] 4. Cuenta inactiva rechazada");
                pruebasCorrectas++;
            } else {
                System.out.println(
                        "[ERROR] 4. Cuenta inactiva");
            }

            // PRUEBA 5: CAMPOS VACIOS
            if (autenticacion.autenticar(
                    "", "").isEmpty()) {

                System.out.println(
                        "[OK] 5. Campos vacíos rechazados");
                pruebasCorrectas++;
            } else {
                System.out.println(
                        "[ERROR] 5. Campos vacíos");
            }

            // PRUEBA 6: REACTIVAR CUENTA
            usuario.setActivo(true);

            if (!usuarioDAO.actualizar(usuario)) {
                throw new IllegalStateException(
                        "No se pudo reactivar la cuenta temporal"
                );
            }

            if (autenticacion.autenticar(
                    correo, password).isPresent()) {

                System.out.println(
                        "[OK] 6. Cuenta reactivada correctamente");
                pruebasCorrectas++;
            } else {
                System.out.println(
                        "[ERROR] 6. Reactivación");
            }

        } catch (Exception e) {

            System.err.println(
                    "[ERROR] Excepción durante la prueba: "
                    + e.getClass().getSimpleName()
            );
            System.err.println(
                    "Detalle: " + e.getMessage()
            );

            if (e.getCause() != null) {
                System.err.println(
                        "Causa: " + e.getCause().getMessage()
                );
            }

        } finally {

            boolean limpiezaCorrecta =
                    limpiarUsuarioTemporal(correo, nombre);

            if (limpiezaCorrecta) {
                System.out.println(
                        "[OK] Limpieza del usuario temporal");
            } else if (registrado) {
                System.err.println(
                        "[AVISO] Revisar usuario temporal en Azure SQL");
            }

            System.out.println("==============================");

            if (pruebasCorrectas == 6 && limpiezaCorrecta) {
                System.out.println(
                        "TODAS LAS PRUEBAS EXITOSAS");
            } else {
                System.out.println(
                        "PRUEBAS COMPLETADAS: "
                        + pruebasCorrectas + "/6"
                );
            }

            System.out.println("==============================");
        }
    }

    private boolean limpiarUsuarioTemporal(
            String correo, String nombre) {

        String sql = """
            DELETE FROM dbo.Usuarios
            WHERE correo = ? AND nombre = ?
            """;

        try (Connection conexion =
                    dataSource.getConnection();
             PreparedStatement ps =
                    conexion.prepareStatement(sql)) {

            ps.setString(1, correo);
            ps.setString(2, nombre);

            int filas = ps.executeUpdate();
            return filas == 1;

        } catch (SQLException e) {

            System.err.println(
                    "[ERROR] Limpieza: " + e.getMessage()
            );
            return false;
        }
    }
}
