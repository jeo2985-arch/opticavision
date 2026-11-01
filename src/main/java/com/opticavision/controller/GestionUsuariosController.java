package com.opticavision.controller;

import com.opticavision.service.GestionUsuariosService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gestion-usuarios")
public class GestionUsuariosController {
    private final GestionUsuariosService service;

    public GestionUsuariosController(GestionUsuariosService service) {
        this.service = service;
    }

    @GetMapping("/contexto")
    public Map<String, Object> contexto(Authentication auth) {
        return service.contexto(auth.getName());
    }

    @GetMapping("/vendedores")
    public List<Map<String, Object>> vendedores(Authentication auth) {
        return service.vendedores(auth.getName());
    }

    @PostMapping("/vendedores")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> crear(@RequestBody Map<String, Object> datos, Authentication auth) {
        return service.crear(datos, auth.getName());
    }

    @PutMapping("/vendedores/{id}/permiso")
    public Map<String, Object> permiso(@PathVariable int id, @RequestBody Map<String, Boolean> datos, Authentication auth) {
        return service.cambiarPermiso(id, Boolean.TRUE.equals(datos.get("puedeCrearUsuarios")), auth.getName());
    }
}
