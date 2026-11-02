package com.opticavision.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
public class FacturaPdfController {

    private final JdbcTemplate jdbc;
    private static final PDType1Font NORMAL = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDType1Font NEGRITA = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

    public FacturaPdfController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping(value = "/vendedor/facturas/{idVenta}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generarFactura(@PathVariable int idVenta, Authentication autenticacion) throws IOException {
        if (autenticacion == null || autenticacion.getAuthorities().stream().noneMatch(
                a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_VENDEDOR"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No autorizado");
        }
        if (idVenta < 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Venta inválida");
        }

        List<Map<String, Object>> ventas = jdbc.queryForList("""
                SELECT v.idVenta, v.fechaVenta, v.subtotal, v.impuesto, v.total,
                       v.estado, c.nombre AS cliente, c.dni, c.correo
                FROM dbo.Ventas v
                INNER JOIN dbo.Clientes c ON c.idCliente = v.idCliente
                WHERE v.idVenta = ?
                """, idVenta);
        if (ventas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta no encontrada");
        }
        Map<String, Object> venta = ventas.get(0);

        List<Map<String, Object>> detalles = jdbc.queryForList("""
                SELECT p.nombre AS producto, d.cantidad, d.precioUnitario, d.subtotal
                FROM dbo.DetalleVentas d
                INNER JOIN dbo.Productos p ON p.idProducto = d.idProducto
                WHERE d.idVenta = ?
                ORDER BY d.idDetalle
                """, idVenta);

        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try (PDDocument documento = new PDDocument()) {
            PDPage pagina = new PDPage(PDRectangle.A4);
            documento.addPage(pagina);
            PDPageContentStream lienzo = new PDPageContentStream(documento, pagina);
            float y = 780;

            escribir(lienzo, NEGRITA, 18, 45, y, "OPTICAVISION");
            y -= 27;
            escribir(lienzo, NEGRITA, 13, 45, y, "Comprobante de venta N. " + idVenta);
            y -= 24;
            escribir(lienzo, NORMAL, 10, 45, y, "Fecha: " + texto(venta.get("fechaVenta")));
            y -= 17;
            escribir(lienzo, NORMAL, 10, 45, y, "Cliente: " + recortar(texto(venta.get("cliente")), 78));
            y -= 17;
            escribir(lienzo, NORMAL, 10, 45, y, "DNI: " + texto(venta.get("dni")));
            y -= 17;
            escribir(lienzo, NORMAL, 10, 45, y, "Correo: " + recortar(texto(venta.get("correo")), 75));
            y -= 17;
            escribir(lienzo, NORMAL, 10, 45, y, "Estado: " + texto(venta.get("estado")));
            y -= 33;

            y = encabezadoTabla(lienzo, y);

            for (Map<String, Object> detalle : detalles) {
                if (y < 105) {
                    lienzo.close();
                    pagina = new PDPage(PDRectangle.A4);
                    documento.addPage(pagina);
                    lienzo = new PDPageContentStream(documento, pagina);
                    y = encabezadoTabla(lienzo, 775);
                }
                escribir(lienzo, NORMAL, 9, 45, y, recortar(texto(detalle.get("producto")), 52));
                escribir(lienzo, NORMAL, 9, 365, y, texto(detalle.get("cantidad")));
                escribir(lienzo, NORMAL, 9, 410, y, dinero(detalle.get("precioUnitario")));
                escribir(lienzo, NORMAL, 9, 500, y, dinero(detalle.get("subtotal")));
                y -= 22;
            }

            if (y < 145) {
                lienzo.close();
                pagina = new PDPage(PDRectangle.A4);
                documento.addPage(pagina);
                lienzo = new PDPageContentStream(documento, pagina);
                y = 775;
            }

            y -= 12;
            linea(lienzo, 45, y, 550, y);
            y -= 22;
            escribir(lienzo, NORMAL, 11, 400, y, "Subtotal: L " + dinero(venta.get("subtotal")));
            y -= 19;
            escribir(lienzo, NORMAL, 11, 400, y, "Impuesto: L " + dinero(venta.get("impuesto")));
            y -= 22;
            escribir(lienzo, NEGRITA, 13, 400, y, "TOTAL: L " + dinero(venta.get("total")));
            y -= 34;
            escribir(lienzo, NORMAL, 9, 45, y, "Gracias por su compra en OpticaVision.");
            lienzo.close();
            documento.save(salida);
        }

        HttpHeaders cabeceras = new HttpHeaders();
        cabeceras.setContentDisposition(ContentDisposition.inline()
                .filename("factura-venta-" + idVenta + ".pdf")
                .build());
        cabeceras.setCacheControl("no-store");
        return ResponseEntity.ok().headers(cabeceras).contentType(MediaType.APPLICATION_PDF)
                .body(salida.toByteArray());
    }

    private float encabezadoTabla(PDPageContentStream lienzo, float y) throws IOException {
        escribir(lienzo, NEGRITA, 10, 45, y, "PRODUCTO");
        escribir(lienzo, NEGRITA, 10, 365, y, "CANT.");
        escribir(lienzo, NEGRITA, 10, 410, y, "PRECIO");
        escribir(lienzo, NEGRITA, 10, 500, y, "IMPORTE");
        linea(lienzo, 45, y - 8, 550, y - 8);
        return y - 27;
    }

    private void escribir(PDPageContentStream lienzo, PDType1Font fuente,
            int tamano, float x, float y, String valor) throws IOException {
        lienzo.beginText();
        lienzo.setFont(fuente, tamano);
        lienzo.newLineAtOffset(x, y);
        lienzo.showText(limpiar(valor));
        lienzo.endText();
    }

    private void linea(PDPageContentStream lienzo, float x1, float y1, float x2, float y2)
            throws IOException {
        lienzo.moveTo(x1, y1);
        lienzo.lineTo(x2, y2);
        lienzo.stroke();
    }

    private String dinero(Object valor) {
        return valor == null ? "0.00" : new BigDecimal(valor.toString()).setScale(2).toPlainString();
    }

    private String texto(Object valor) {
        return valor == null ? "" : valor.toString();
    }

    private String recortar(String texto, int maximo) {
        return texto.length() <= maximo ? texto : texto.substring(0, maximo - 3) + "...";
    }

    private String limpiar(String texto) {
        return texto.replaceAll("[^\\x20-\\x7E\\xA0-\\xFF]", " ");
    }
}
