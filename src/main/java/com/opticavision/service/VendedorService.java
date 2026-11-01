package com.opticavision.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VendedorService {

    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder;
    private final BigDecimal tasaImpuesto;

    public VendedorService(JdbcTemplate jdbc, PasswordEncoder encoder,
            @Value("${opticavision.impuesto.porcentaje:15}") BigDecimal porcentaje) {
        this.jdbc = jdbc;
        this.encoder = encoder;
        if (porcentaje.compareTo(BigDecimal.ZERO) < 0 || porcentaje.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("El porcentaje de impuesto debe estar entre 0 y 100.");
        }
        this.tasaImpuesto = porcentaje.divide(new BigDecimal("100"), 6, RoundingMode.HALF_UP);
    }

    public Map<String, Object> resumen() {
        return Map.of(
                "productos", numero("SELECT COUNT(*) FROM dbo.Productos WHERE activo = 1"),
                "stockBajo", numero("SELECT COUNT(*) FROM dbo.Productos WHERE activo = 1 AND stock < 5"),
                "clientes", numero("SELECT COUNT(*) FROM dbo.Clientes"),
                "ventas", numero("SELECT COUNT(*) FROM dbo.Ventas"),
                "impuestoPorcentaje", tasaImpuesto.multiply(new BigDecimal("100"))
        );
    }

    public List<Map<String, Object>> productos() {
        return jdbc.queryForList("""
                SELECT idProducto, nombre, categoria, marca, material, forma, precio, stock, descripcion, activo
                FROM dbo.Productos ORDER BY idProducto DESC
                """);
    }

    @Transactional
    public Map<String, Object> agregarProducto(Map<String, Object> d) {
        String nombre = requerido(d, "nombre", 150);
        String categoria = categoria(d);
        BigDecimal precio = decimalPositivo(d, "precio");
        int stock = enteroNoNegativo(d, "stock");
        int id = idNuevo("""
                INSERT INTO dbo.Productos (nombre, categoria, marca, material, forma, precio, stock, descripcion, activo)
                OUTPUT INSERTED.idProducto VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, nombre, categoria, opcional(d, "marca", 100), opcional(d, "material", 100),
                opcional(d, "forma", 50), precio, stock, opcional(d, "descripcion", 500), booleano(d, "activo", true));
        return Map.of("idProducto", id, "mensaje", "Producto registrado correctamente.");
    }

    @Transactional
    public Map<String, Object> actualizarProducto(int id, Map<String, Object> d) {
        int filas = jdbc.update("""
                UPDATE dbo.Productos SET nombre=?, categoria=?, marca=?, material=?, forma=?,
                precio=?, stock=?, descripcion=?, activo=? WHERE idProducto=?
                """, requerido(d, "nombre", 150), categoria(d), opcional(d, "marca", 100),
                opcional(d, "material", 100), opcional(d, "forma", 50), decimalPositivo(d, "precio"),
                enteroNoNegativo(d, "stock"), opcional(d, "descripcion", 500), booleano(d, "activo", true), id);
        existe(filas, "Producto no encontrado.");
        return Map.of("mensaje", "Producto actualizado correctamente.");
    }

    public List<Map<String, Object>> clientes() {
        return jdbc.queryForList("""
                SELECT c.idCliente, c.idUsuario, c.nombre, c.dni, c.telefono, c.correo, u.activo
                FROM dbo.Clientes c JOIN dbo.Usuarios u ON u.id=c.idUsuario
                ORDER BY c.idCliente DESC
                """);
    }

    @Transactional
    public Map<String, Object> registrarCliente(Map<String, Object> d) {
        String nombre = requerido(d, "nombre", 100);
        String correo = requerido(d, "correo", 150).toLowerCase();
        if (!correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("El correo no tiene un formato válido.");
        }
        String password = requerido(d, "password", 200);
        if (password.length() < 12) {
            throw new IllegalArgumentException("La contraseña inicial debe tener al menos 12 caracteres.");
        }
        String dni = requerido(d, "dni", 20);
        String telefono = requerido(d, "telefono", 20);
        int idUsuario = idNuevo("""
                INSERT INTO dbo.Usuarios (nombre, correo, passwordHash, rol, activo)
                OUTPUT INSERTED.id VALUES (?, ?, ?, 'CLIENTE', 1)
                """, nombre, correo, encoder.encode(password));
        int idCliente = idNuevo("""
                INSERT INTO dbo.Clientes (idUsuario, nombre, dni, telefono, correo)
                OUTPUT INSERTED.idCliente VALUES (?, ?, ?, ?, ?)
                """, idUsuario, nombre, dni, telefono, correo);
        return Map.of("idCliente", idCliente, "mensaje", "Cliente registrado correctamente.");
    }

    @Transactional
    public Map<String, Object> actualizarCliente(int id, Map<String, Object> d) {
        String nombre = requerido(d, "nombre", 100);
        String correo = requerido(d, "correo", 150).toLowerCase();
        if (!correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("El correo no tiene un formato válido.");
        }
        int idUsuario = buscarEntero("SELECT idUsuario FROM dbo.Clientes WHERE idCliente=?", id);
        existe(jdbc.update("""
                UPDATE dbo.Clientes SET nombre=?, dni=?, telefono=?, correo=? WHERE idCliente=?
                """, nombre, requerido(d, "dni", 20), requerido(d, "telefono", 20), correo, id),
                "Cliente no encontrado.");
        jdbc.update("UPDATE dbo.Usuarios SET nombre=?, correo=? WHERE id=?", nombre, correo, idUsuario);
        return Map.of("mensaje", "Cliente actualizado correctamente.");
    }

    public List<Map<String, Object>> mediciones(int idCliente) {
        buscarEntero("SELECT idCliente FROM dbo.Clientes WHERE idCliente=?", idCliente);
        return jdbc.queryForList("""
                SELECT idMedicion, idCliente, fechaMedicion, esferaOD, cilindroOD, ejeOD, adicionOD,
                esferaOI, cilindroOI, ejeOI, adicionOI, observaciones
                FROM dbo.Mediciones WHERE idCliente=? ORDER BY fechaMedicion DESC, idMedicion DESC
                """, idCliente);
    }

    @Transactional
    public Map<String, Object> registrarMedicion(int idCliente, Map<String, Object> d) {
        buscarEntero("SELECT idCliente FROM dbo.Clientes WHERE idCliente=?", idCliente);
        LocalDate fecha;
        try {
            fecha = LocalDate.parse(requerido(d, "fechaMedicion", 10));
        } catch (RuntimeException e) {
            throw new IllegalArgumentException("La fecha de medición no es válida.");
        }
        int id = idNuevo("""
                INSERT INTO dbo.Mediciones
                (idCliente, fechaMedicion, esferaOD, cilindroOD, ejeOD, adicionOD,
                 esferaOI, cilindroOI, ejeOI, adicionOI, observaciones)
                OUTPUT INSERTED.idMedicion VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, idCliente, Date.valueOf(fecha), graduacion(d, "esferaOD"), graduacion(d, "cilindroOD"),
                eje(d, "ejeOD"), graduacion(d, "adicionOD"), graduacion(d, "esferaOI"),
                graduacion(d, "cilindroOI"), eje(d, "ejeOI"), graduacion(d, "adicionOI"),
                opcional(d, "observaciones", 500));
        return Map.of("idMedicion", id, "mensaje", "Medición registrada correctamente.");
    }

    public List<Map<String, Object>> ventas() {
        return jdbc.queryForList("""
                SELECT v.idVenta, v.fechaVenta, v.idCliente, c.nombre AS cliente,
                v.idVendedor, v.idMedicion, v.subtotal, v.impuesto, v.total, v.estado
                FROM dbo.Ventas v JOIN dbo.Clientes c ON c.idCliente=v.idCliente
                ORDER BY v.idVenta DESC
                """);
    }

    public List<Map<String, Object>> detalles(int idVenta) {
        buscarEntero("SELECT idVenta FROM dbo.Ventas WHERE idVenta=?", idVenta);
        return jdbc.queryForList("""
                SELECT d.idDetalle, d.idVenta, d.idProducto, p.nombre AS producto,
                d.cantidad, d.precioUnitario, d.subtotal
                FROM dbo.DetalleVentas d JOIN dbo.Productos p ON p.idProducto=d.idProducto
                WHERE d.idVenta=? ORDER BY d.idDetalle
                """, idVenta);
    }

    @Transactional
    public Map<String, Object> registrarVenta(Map<String, Object> d, String correoVendedor) {
        int idVendedor = buscarEntero("""
                SELECT id FROM dbo.Usuarios WHERE correo=? AND rol='VENDEDOR' AND activo=1
                """, correoVendedor);
        int idCliente = enteroPositivo(d, "idCliente");
        buscarEntero("SELECT idCliente FROM dbo.Clientes WHERE idCliente=?", idCliente);
        Integer idMedicion = opcionalEntero(d, "idMedicion");
        if (idMedicion != null) {
            buscarEntero("SELECT idMedicion FROM dbo.Mediciones WHERE idMedicion=? AND idCliente=?", idMedicion, idCliente);
        }
        Object valor = d.get("items");
        if (!(valor instanceof List<?> items) || items.isEmpty()) {
            throw new IllegalArgumentException("La venta debe incluir al menos un producto.");
        }
        if (items.size() > 100) {
            throw new IllegalArgumentException("La venta supera el máximo de 100 productos distintos.");
        }
        List<Linea> lineas = new ArrayList<>();
        Set<Integer> vistos = new HashSet<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Object item : items) {
            if (!(item instanceof Map<?, ?> valores)) {
                throw new IllegalArgumentException("Producto de venta inválido.");
            }
            int idProducto = enteroPositivo(valores, "idProducto");
            int cantidad = enteroPositivo(valores, "cantidad");
            if (!vistos.add(idProducto)) {
                throw new IllegalArgumentException("No repitas un producto dentro de la venta.");
            }
            BigDecimal precio = buscarDecimal("""
                    SELECT precio FROM dbo.Productos WHERE idProducto=? AND activo=1
                    """, idProducto);
            int filas = jdbc.update("""
                    UPDATE dbo.Productos SET stock=stock-?
                    WHERE idProducto=? AND activo=1 AND stock>=?
                    """, cantidad, idProducto, cantidad);
            if (filas != 1) {
                throw new IllegalArgumentException("Stock insuficiente para el producto " + idProducto + ".");
            }
            BigDecimal importe = precio.multiply(BigDecimal.valueOf(cantidad)).setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(importe);
            lineas.add(new Linea(idProducto, cantidad, precio, importe));
        }
        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal impuesto = subtotal.multiply(tasaImpuesto).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(impuesto);
        int idVenta = idNuevo("""
                INSERT INTO dbo.Ventas (idCliente, idVendedor, idMedicion, subtotal, impuesto, total, estado)
                OUTPUT INSERTED.idVenta VALUES (?, ?, ?, ?, ?, ?, 'COMPLETADA')
                """, idCliente, idVendedor, idMedicion, subtotal, impuesto, total);
        for (Linea linea : lineas) {
            jdbc.update("""
                    INSERT INTO dbo.DetalleVentas (idVenta, idProducto, cantidad, precioUnitario, subtotal)
                    VALUES (?, ?, ?, ?, ?)
                    """, idVenta, linea.idProducto(), linea.cantidad(), linea.precio(), linea.subtotal());
        }
        return Map.of("idVenta", idVenta, "subtotal", subtotal, "impuesto", impuesto, "total", total,
                "mensaje", "Venta registrada correctamente.");
    }

    private record Linea(int idProducto, int cantidad, BigDecimal precio, BigDecimal subtotal) { }

    private int idNuevo(String sql, Object... parametros) {
        Integer id = jdbc.queryForObject(sql, Integer.class, parametros);
        if (id == null || id < 1) {
            throw new IllegalStateException("No se obtuvo el ID del registro.");
        }
        return id;
    }

    private int buscarEntero(String sql, Object... parametros) {
        List<Integer> ids = jdbc.query(sql, (rs, fila) -> rs.getInt(1), parametros);
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("No se encontró el registro solicitado.");
        }
        return ids.get(0);
    }

    private BigDecimal buscarDecimal(String sql, Object... parametros) {
        List<BigDecimal> precios = jdbc.query(sql, (rs, fila) -> rs.getBigDecimal(1), parametros);
        if (precios.isEmpty() || precios.get(0) == null) {
            throw new IllegalArgumentException("Producto inexistente o inactivo.");
        }
        return precios.get(0);
    }

    private int numero(String sql) {
        Integer n = jdbc.queryForObject(sql, Integer.class);
        return n == null ? 0 : n;
    }

    private void existe(int filas, String mensaje) {
        if (filas != 1) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private String requerido(Map<?, ?> datos, String campo, int max) {
        Object v = datos.get(campo);
        if (!(v instanceof String s) || s.isBlank() || s.trim().length() > max) {
            throw new IllegalArgumentException("Campo obligatorio o longitud inválida: " + campo);
        }
        return s.trim();
    }

    private String opcional(Map<?, ?> datos, String campo, int max) {
        Object v = datos.get(campo);
        if (v == null || v.toString().isBlank()) {
            return null;
        }
        String s = v.toString().trim();
        if (s.length() > max) {
            throw new IllegalArgumentException("Campo demasiado largo: " + campo);
        }
        return s;
    }

    private boolean booleano(Map<?, ?> datos, String campo, boolean porDefecto) {
        Object v = datos.get(campo);
        if (v == null) {
            return porDefecto;
        }
        if (!(v instanceof Boolean b)) {
            throw new IllegalArgumentException("Valor booleano inválido: " + campo);
        }
        return b;
    }

    private String categoria(Map<?, ?> datos) {
        String c = requerido(datos, "categoria", 20);
        if (!Set.of("MONTURA", "CRISTAL", "ACCESORIO").contains(c)) {
            throw new IllegalArgumentException("Categoría de producto inválida.");
        }
        return c;
    }

    private BigDecimal decimalPositivo(Map<?, ?> datos, String campo) {
        try {
            Object v = datos.get(campo);
            BigDecimal n = new BigDecimal(String.valueOf(v)).setScale(2, RoundingMode.HALF_UP);
            if (n.compareTo(BigDecimal.ZERO) < 0 || n.compareTo(new BigDecimal("9999999999.99")) > 0) {
                throw new NumberFormatException();
            }
            return n;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Importe no válido: " + campo);
        }
    }

    private BigDecimal graduacion(Map<?, ?> datos, String campo) {
        Object v = datos.get(campo);
        if (v == null || v.toString().isBlank()) {
            return null;
        }
        try {
            BigDecimal n = new BigDecimal(v.toString()).setScale(2, RoundingMode.HALF_UP);
            if (n.abs().compareTo(new BigDecimal("999.99")) > 0) {
                throw new NumberFormatException();
            }
            return n;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Medida no válida: " + campo);
        }
    }

    private Integer eje(Map<?, ?> datos, String campo) {
        Object v = datos.get(campo);
        if (v == null || v.toString().isBlank()) {
            return null;
        }
        try {
            int n = Integer.parseInt(v.toString());
            if (n < 0 || n > 180) {
                throw new NumberFormatException();
            }
            return n;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El eje debe estar entre 0 y 180: " + campo);
        }
    }

    private Integer opcionalEntero(Map<?, ?> datos, String campo) {
        Object v = datos.get(campo);
        if (v == null || v.toString().isBlank()) {
            return null;
        }
        return enteroPositivo(datos, campo);
    }

    private int enteroPositivo(Map<?, ?> datos, String campo) {
        try {
            Object v = datos.get(campo);
            int n = Integer.parseInt(String.valueOf(v));
            if (n < 1) {
                throw new NumberFormatException();
            }
            return n;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Número positivo requerido: " + campo);
        }
    }

    private int enteroNoNegativo(Map<?, ?> datos, String campo) {
        try {
            Object v = datos.get(campo);
            int n = Integer.parseInt(String.valueOf(v));
            if (n < 0) {
                throw new NumberFormatException();
            }
            return n;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Número no negativo requerido: " + campo);
        }
    }
}
