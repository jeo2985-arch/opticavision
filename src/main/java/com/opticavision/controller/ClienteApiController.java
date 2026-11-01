package com.opticavision.controller;

import com.opticavision.service.ClientePortalService;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cliente")
public class ClienteApiController {
    private final ClientePortalService service;
    public ClienteApiController(ClientePortalService service) { this.service = service; }
    @GetMapping("/csrf")
    public Map<String,String> csrf(CsrfToken token) { return Map.of("token", token.getToken()); }
    @GetMapping("/perfil")
    public Map<String,Object> perfil(Authentication auth) { return service.miPerfil(auth.getName()); }
    @GetMapping("/catalogo")
    public List<Map<String,Object>> catalogo() { return service.catalogo(); }
    @GetMapping("/mediciones")
    public List<Map<String,Object>> mediciones(Authentication auth) { return service.misMediciones(auth.getName()); }
    @GetMapping("/compras")
    public List<Map<String,Object>> compras(Authentication auth) { return service.misCompras(auth.getName()); }
}
