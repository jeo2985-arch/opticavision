/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.opticavision.config;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
/**
 *
 * @author Jairo Ortega
 */
@Component("permisoCrearUsuarios")
public class PermisoCrearUsuarios {

    private final JdbcTemplate jdbcTemplate;

    public PermisoCrearUsuarios(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean permitido(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {
            return false;
        }

        boolean esAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority()
                        .equals("ROLE_ADMIN"));

        boolean esVendedor = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority()
                        .equals("ROLE_VENDEDOR"));

        if (!esAdmin && !esVendedor) {
            return false;
        }

        String sql = """
            SELECT COUNT(*)
            FROM dbo.Usuarios
            WHERE correo = ?
              AND activo = 1
              AND (
                  rol = 'ADMIN'
                  OR (
                      rol = 'VENDEDOR'
                      AND puedeCrearUsuarios = 1
                  )
              )
            """;

        try {
            Integer cantidad = jdbcTemplate.queryForObject(
                    sql,
                    Integer.class,
                    authentication.getName()
            );

            return cantidad != null && cantidad > 0;

        } catch (Exception e) {
            return false;
        }
    }
}
