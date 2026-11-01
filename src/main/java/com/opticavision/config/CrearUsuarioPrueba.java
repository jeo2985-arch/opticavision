
package com.opticavision.config;

import com.opticavision.dao.UsuarioDAO;
import com.opticavision.model.Usuario;
import com.opticavision.service.SeguridadPassword;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CrearUsuarioPrueba implements CommandLineRunner {

    private final UsuarioDAO usuarioDAO;
    private final SeguridadPassword seguridadPassword;

    public CrearUsuarioPrueba(
            UsuarioDAO usuarioDAO,
            SeguridadPassword seguridadPassword) {

        this.usuarioDAO = usuarioDAO;
        this.seguridadPassword = seguridadPassword;
    }

    @Override
    public void run(String... args) {

        String correo = "vendedor.prueba@example.com";

        if (usuarioDAO.buscarPorCorreo(correo).isPresent()) {

            System.out.println(
                "USUARIO DE PRUEBA YA EXISTE"
            );

            return;
        }

        // Contraseña exclusiva para desarrollo local.
        // No debe utilizarse en producción.
        String password = "OpticaPrueba#2026";

        Usuario usuario = new Usuario();

        usuario.setNombre("Vendedor de Prueba");
        usuario.setCorreo(correo);
        usuario.setPasswordHash(
            seguridadPassword.encriptarPassword(password)
        );
        usuario.setRol("VENDEDOR");
        usuario.setActivo(true);

        boolean registrado = usuarioDAO.registrar(usuario);

        if (registrado) {

            System.out.println(
                "USUARIO VENDEDOR CREADO CORRECTAMENTE"
            );

        } else {

            System.out.println(
                "NO SE PUDO CREAR EL USUARIO"
            );
        }
    }
}
