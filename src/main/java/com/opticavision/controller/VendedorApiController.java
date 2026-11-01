package com.opticavision.controller;

import com.opticavision.service.VendedorService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/vendedor")
public class VendedorApiController {

    private final VendedorService service;

    public VendedorApiController(VendedorService service) {
        this.service = service;
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("token", token.getToken());
    }

    @GetMapping("/resumen")
    public Map<String, Object> resumen() {
        return service.resumen();
    }

    @GetMapping("/productos")
    public List<Map<String, Object>> productos() {
        return service.productos();
    }

    @PostMapping("/productos")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> agregarProducto(@RequestBody Map<String, Object> datos) {
        return service.agregarProducto(datos);
    }

    @PutMapping("/productos/{id}")
    public Map<String, Object> actualizarProducto(@PathVariable int id, @RequestBody Map<String, Object> datos) {
        return service.actualizarProducto(id, datos);
    }

    @GetMapping("/clientes")
    public List<Map<String, Object>> clientes() {
        return service.clientes();
    }

    @PostMapping("/clientes")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> registrarCliente(@RequestBody Map<String, Object> datos) {
        return service.registrarCliente(datos);
    }

    @PutMapping("/clientes/{id}")
    public Map<String, Object> actualizarCliente(@PathVariable int id, @RequestBody Map<String, Object> datos) {
        return service.actualizarCliente(id, datos);
    }

    @GetMapping("/clientes/{id}/mediciones")
    public List<Map<String, Object>> mediciones(@PathVariable int id) {
        return service.mediciones(id);
    }

    @PostMapping("/clientes/{id}/mediciones")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> registrarMedicion(@PathVariable int id, @RequestBody Map<String, Object> datos) {
        return service.registrarMedicion(id, datos);
    }

    @GetMapping("/ventas")
    public List<Map<String, Object>> ventas() {
        return service.ventas();
    }

    @GetMapping("/ventas/{id}/detalles")
    public List<Map<String, Object>> detalles(@PathVariable int id) {
        return service.detalles(id);
    }

    @PostMapping("/ventas")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> registrarVenta(@RequestBody Map<String, Object> datos, Authentication authentication) {
        return service.registrarVenta(datos, authentication.getName());
    }
}
