/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.opticavision.controller;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
/**
 *
 * @author Jairo Ortega
 */
public class VendedorController {
    
    @GetMapping(
        value = "/vendedor/prueba",
        produces = "text/html; charset=UTF-8"
    )
    public String pruebaVendedor(Authentication authentication) {

        String correo = authentication.getName();

        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
                <meta charset="UTF-8">
                <title>ÓpticaVision - Vendedor</title>
            </head>
            <body>
                <h1>Bienvenido a ÓpticaVision</h1>
                <h2>Panel de Vendedor</h2>

                <p>Inicio de sesión correcto.</p>
                <p>Acceso autorizado para el rol VENDEDOR.</p>

                <p>Usuario autenticado:</p>
                <strong>%s</strong>

                <hr>

                <p>
                    Próximamente: inventario,
                    clientes y ventas.
                </p>
            </body>
            </html>
            """.formatted(
                org.springframework.web.util.HtmlUtils.htmlEscape(correo)
            );
    }
}
