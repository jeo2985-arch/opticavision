package com.opticavision.service;

import java.util.List;
import java.util.Map;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ClientePortalService {
    private final JdbcTemplate jdbc;
    public ClientePortalService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public Map<String, Object> miPerfil(String correo) {
        List<Map<String, Object>> lista = jdbc.queryForList("""
                SELECT c.idCliente,c.nombre,c.dni,c.telefono,c.correo
                FROM dbo.Clientes c
                JOIN dbo.Usuarios u ON u.id=c.idUsuario
                WHERE u.correo=? AND u.activo=1 AND u.rol='CLIENTE'
                """, correo);
        if (lista.isEmpty()) throw new IllegalArgumentException("No se encontró el perfil de cliente.");
        return lista.get(0);
    }

    public List<Map<String, Object>> catalogo() {
        return jdbc.queryForList("""
                SELECT idProducto,nombre,categoria,marca,material,forma,precio,stock,descripcion
                FROM dbo.Productos WHERE activo=1 ORDER BY nombre
                """);
    }

    public List<Map<String, Object>> misMediciones(String correo) {
        return jdbc.queryForList("""
                SELECT m.idMedicion,m.fechaMedicion,m.esferaOD,m.cilindroOD,m.ejeOD,m.adicionOD,
                       m.esferaOI,m.cilindroOI,m.ejeOI,m.adicionOI,m.observaciones
                FROM dbo.Mediciones m JOIN dbo.Clientes c ON c.idCliente=m.idCliente
                JOIN dbo.Usuarios u ON u.id=c.idUsuario
                WHERE u.correo=? AND u.activo=1 AND u.rol='CLIENTE'
                ORDER BY m.fechaMedicion DESC,m.idMedicion DESC
                """, correo);
    }

    public List<Map<String, Object>> misCompras(String correo) {
        return jdbc.queryForList("""
                SELECT v.idVenta,v.fechaVenta,v.subtotal,v.impuesto,v.total,v.estado
                FROM dbo.Ventas v JOIN dbo.Clientes c ON c.idCliente=v.idCliente
                JOIN dbo.Usuarios u ON u.id=c.idUsuario
                WHERE u.correo=? AND u.activo=1 AND u.rol='CLIENTE'
                ORDER BY v.idVenta DESC
                """, correo);
    }
}
