/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.opticavision.dao;
import com.opticavision.model.DetalleVenta;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author Jairo Ortega
 */
public interface DetalleVentaDAO {
     
    boolean registrar(DetalleVenta detalleVenta);

    
    Optional<DetalleVenta> buscarPorId(int idDetalle);

   
    List<DetalleVenta> listarPorVenta(int idVenta);

    
    List<DetalleVenta> listarTodos();
}
