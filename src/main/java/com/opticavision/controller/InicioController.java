package com.opticavision.controller;

import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InicioController {
    @GetMapping("/inicio")
    public void inicio(Authentication authentication, HttpServletResponse response) throws IOException {
        boolean admin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).anyMatch("ROLE_ADMIN"::equals);
        boolean vendedor = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).anyMatch("ROLE_VENDEDOR"::equals);
        boolean cliente = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).anyMatch("ROLE_CLIENTE"::equals);
        if (admin) response.sendRedirect("/admin/dashboard.html");
        else if (vendedor) response.sendRedirect("/vendedor/dashboard.html");
        else if (cliente) response.sendRedirect("/cliente/dashboard.html");
        else response.sendError(403);
    }
}
