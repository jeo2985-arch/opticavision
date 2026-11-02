package com.opticavision.controller;

import com.opticavision.service.AmpliacionService;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.HtmlUtils;

@RestController
public class AmpliacionController {

    private final AmpliacionService service;

    public AmpliacionController(AmpliacionService service) {
        this.service = service;
    }

    @GetMapping("/api/vendedor/ampliacion/facturas")
    public List<Map<String, Object>> facturasVendedor() {
        return service.facturasVendedor();
    }

    @GetMapping("/api/cliente/ampliacion/facturas")
    public List<Map<String, Object>> facturasCliente(Authentication auth) {
        return service.facturasCliente(auth.getName());
    }

    @GetMapping(value = "/vendedor/factura/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> facturaVendedor(@PathVariable int id, Authentication auth) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/html;charset=UTF-8"))
                .header("Cache-Control", "no-store").body(html(service.factura(id, auth.getName(), true)));
    }

    @GetMapping(value = "/cliente/factura/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> facturaCliente(@PathVariable int id, Authentication auth) {
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/html;charset=UTF-8"))
                .header("Cache-Control", "no-store").body(html(service.factura(id, auth.getName(), false)));
    }

    private static String seguro(Object v) {
        return HtmlUtils.htmlEscape(v == null ? "" : String.valueOf(v));
    }

    private static String lempiras(Object valor) {
        NumberFormat formato = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-HN"));
        BigDecimal n = new BigDecimal(String.valueOf(valor == null ? "0" : valor));
        return seguro(formato.format(n));
    }

    private String html(AmpliacionService.DatosFactura factura) {
        Map<String, Object> e = factura.encabezado();
        StringBuilder filas = new StringBuilder();
        for (Map<String, Object> item : factura.lineas()) {
            filas.append("<tr><td>").append(seguro(item.get("producto")))
                .append("</td><td>").append(seguro(item.get("cantidad")))
                .append("</td><td>").append(lempiras(item.get("precioUnitario")))
                .append("</td><td>").append(lempiras(item.get("subtotal")))
                .append("</td></tr>");
        }
        return """
                <!doctype html>
                <html lang="es"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
                <title>Comprobante de venta</title><style>
                body{font:16px Arial,sans-serif;color:#19314b;max-width:860px;margin:35px auto;padding:16px;background:#f4f7fb}
                main{background:white;padding:32px;border-radius:12px}h1{color:#0a5788}p{line-height:1.5}
                table{border-collapse:collapse;width:100%%;margin-top:24px}td,th{text-align:left;padding:12px;border-bottom:1px solid #dbe3ec}
                .totales{text-align:right;margin-top:24px}.totales p{margin:8px 0}.alerta{font-size:13px;color:#7b4d00;background:#fff4d7;padding:12px;border-radius:8px}
                button{background:#12618b;color:#fff;border:0;border-radius:6px;padding:12px 20px;cursor:pointer}
                @media print{body{background:white;margin:0}main{padding:0}button{display:none}}
                </style></head><body><main>
                <h1>ÓpticaVision</h1><h2>Comprobante de venta N.º %s</h2>
                <p>Fecha: %s</p><p>Cliente: %s</p><p>DNI: %s</p><p>Correo: %s</p>
                <p>Atendido por: %s</p><p>Estado: %s</p>
                <table><thead><tr><th>Producto</th><th>Cantidad</th><th>Precio unitario</th><th>Importe</th></tr></thead>
                <tbody>%s</tbody></table>
                <div class="totales"><p>Subtotal: %s</p><p>Impuesto registrado: %s</p><h2>Total: %s</h2></div>
                <p class="alerta">Comprobante interno generado desde una venta registrada. No sustituye una factura fiscal autorizada.</p>
                <button onclick="window.print()">Imprimir o guardar como PDF</button>
                </main></body></html>
                """.formatted(seguro(e.get("idVenta")), seguro(e.get("fechaVenta")), seguro(e.get("cliente")),
                seguro(e.get("dni")), seguro(e.get("correoCliente")), seguro(e.get("vendedor")),
                seguro(e.get("estado")), filas, lempiras(e.get("subtotal")),
                lempiras(e.get("impuesto")), lempiras(e.get("total")));
    }
}
