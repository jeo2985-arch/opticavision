package com.opticavision.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Venta {
    private int idVenta, idCliente, idVendedor;
    private Integer idMedicion;
    private LocalDateTime fechaVenta;
    private BigDecimal subtotal, impuesto, total;
    private String estado;
    public Venta() {}
    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int v) { idVenta = v; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int v) { idCliente = v; }
    public int getIdVendedor() { return idVendedor; }
    public void setIdVendedor(int v) { idVendedor = v; }
    public Integer getIdMedicion() { return idMedicion; }
    public void setIdMedicion(Integer v) { idMedicion = v; }
    public LocalDateTime getFechaVenta() { return fechaVenta; }
    public void setFechaVenta(LocalDateTime v) { fechaVenta = v; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal v) { subtotal = v; }
    public BigDecimal getImpuesto() { return impuesto; }
    public void setImpuesto(BigDecimal v) { impuesto = v; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal v) { total = v; }
    public String getEstado() { return estado; }
    public void setEstado(String v) { estado = v; }
}
