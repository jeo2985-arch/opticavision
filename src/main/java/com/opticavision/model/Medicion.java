package com.opticavision.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Medicion {
    private int idMedicion;
    private int idCliente;
    private LocalDate fechaMedicion;
    private BigDecimal esferaOD, cilindroOD, adicionOD, esferaOI, cilindroOI, adicionOI;
    private Integer ejeOD, ejeOI;
    private String observaciones;

    public Medicion() {}
    public int getIdMedicion() { return idMedicion; }
    public void setIdMedicion(int value) { idMedicion = value; }
    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int value) { idCliente = value; }
    public LocalDate getFechaMedicion() { return fechaMedicion; }
    public void setFechaMedicion(LocalDate value) { fechaMedicion = value; }
    public BigDecimal getEsferaOD() { return esferaOD; }
    public void setEsferaOD(BigDecimal value) { esferaOD = value; }
    public BigDecimal getCilindroOD() { return cilindroOD; }
    public void setCilindroOD(BigDecimal value) { cilindroOD = value; }
    public BigDecimal getAdicionOD() { return adicionOD; }
    public void setAdicionOD(BigDecimal value) { adicionOD = value; }
    public BigDecimal getEsferaOI() { return esferaOI; }
    public void setEsferaOI(BigDecimal value) { esferaOI = value; }
    public BigDecimal getCilindroOI() { return cilindroOI; }
    public void setCilindroOI(BigDecimal value) { cilindroOI = value; }
    public BigDecimal getAdicionOI() { return adicionOI; }
    public void setAdicionOI(BigDecimal value) { adicionOI = value; }
    public Integer getEjeOD() { return ejeOD; }
    public void setEjeOD(Integer value) { ejeOD = value; }
    public Integer getEjeOI() { return ejeOI; }
    public void setEjeOI(Integer value) { ejeOI = value; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String value) { observaciones = value; }
}
