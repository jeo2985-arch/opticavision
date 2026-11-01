package com.opticavision.service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AmpliacionService {

    private final JdbcTemplate jdbc;

    public AmpliacionService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Map<String, Object>> facturasVendedor() {
        return jdbc.queryForList("""
                SELECT v.idVenta, v.fechaVenta, c.nombre AS cliente,
                       v.subtotal, v.impuesto, v.total, v.estado
                FROM dbo.Ventas v JOIN dbo.Clientes c ON c.idCliente=v.idCliente
                ORDER BY v.idVenta DESC
                """);
    }

    public List<Map<String, Object>> facturasCliente(String correo) {
        return jdbc.queryForList("""
                SELECT v.idVenta, v.fechaVenta, v.subtotal, v.impuesto, v.total, v.estado
                FROM dbo.Ventas v
                JOIN dbo.Clientes c ON c.idCliente=v.idCliente
                JOIN dbo.Usuarios u ON u.id=c.idUsuario
                WHERE u.correo=? AND u.activo=1 AND u.rol='CLIENTE'
                ORDER BY v.idVenta DESC
                """, correo);
    }

    public DatosFactura factura(int idVenta, String correo, boolean vendedor) {
        String sql = """
                SELECT v.idVenta, v.fechaVenta, v.subtotal, v.impuesto, v.total, v.estado,
                       c.nombre AS cliente, c.dni, c.correo AS correoCliente,
                       u.nombre AS vendedor
                FROM dbo.Ventas v
                JOIN dbo.Clientes c ON c.idCliente=v.idCliente
                JOIN dbo.Usuarios u ON u.id=v.idVendedor
                WHERE v.idVenta=?
                """;
        if (!vendedor) {
            sql += """
                    AND EXISTS (
                       SELECT 1 FROM dbo.Clientes c2
                       JOIN dbo.Usuarios uc ON uc.id=c2.idUsuario
                       WHERE c2.idCliente=v.idCliente AND uc.correo=? AND uc.activo=1 AND uc.rol='CLIENTE'
                    )
                    """;
        }
        List<Map<String, Object>> encabezados = vendedor
                ? jdbc.queryForList(sql, idVenta)
                : jdbc.queryForList(sql, idVenta, correo);
        if (encabezados.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Factura no encontrada.");
        }
        List<Map<String, Object>> lineas = jdbc.queryForList("""
                SELECT d.cantidad, p.nombre AS producto, d.precioUnitario, d.subtotal
                FROM dbo.DetalleVentas d
                JOIN dbo.Productos p ON p.idProducto=d.idProducto
                WHERE d.idVenta=? ORDER BY d.idDetalle
                """, idVenta);
        return new DatosFactura(encabezados.get(0), lineas);
    }

    public record DatosFactura(Map<String, Object> encabezado, List<Map<String, Object>> lineas) { }
}
