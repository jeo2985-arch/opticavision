package com.opticavision.controller;

import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RolActualController {

    @GetMapping("/vendedor/rol-actual")
    public Map<String, Boolean> consultarRol(Authentication authentication) {

        boolean esAdmin = authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                        .anyMatch(autoridad ->
                                autoridad.getAuthority().equals("ROLE_ADMIN"));

        return Map.of("esAdmin", esAdmin);
    }
}