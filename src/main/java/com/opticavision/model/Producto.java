package com.opticavision.model;

import java.math.BigDecimal;

public class Producto {
    private int idProducto;
    private String nombre;
    private String categoria;
    private String marca;
    private String material;
    private String forma;
    private BigDecimal precio;
    private int stock;
    private String descripcion;
    private boolean activo;

    public Producto() {}
    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int value) { idProducto = value; }
    public String getNombre() { return nombre; }
    public void setNombre(String value) { nombre = value; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String value) { categoria = value; }
    public String getMarca() { return marca; }
    public void setMarca(String value) { marca = value; }
    public String getMaterial() { return material; }
    public void setMaterial(String value) { material = value; }
    public String getForma() { return forma; }
    public void setForma(String value) { forma = value; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal value) { precio = value; }
    public int getStock() { return stock; }
    public void setStock(int value) { stock = value; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String value) { descripcion = value; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean value) { activo = value; }
}
