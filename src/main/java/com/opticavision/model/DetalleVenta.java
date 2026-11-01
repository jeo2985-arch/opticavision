package com.opticavision.model;

import java.math.BigDecimal;

public class DetalleVenta {
    private int idDetalle, idVenta, idProducto, cantidad;
    private BigDecimal precioUnitario, subtotal;
    public DetalleVenta() {}
    public int getIdDetalle() { return idDetalle; }
    public void setIdDetalle(int v) { idDetalle = v; }
    public int getIdVenta() { return idVenta; }
    public void setIdVenta(int v) { idVenta = v; }
    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int v) { idProducto = v; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int v) { cantidad = v; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal v) { precioUnitario = v; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal v) { subtotal = v; }
}
