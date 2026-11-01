/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.opticavision.dao;
import com.opticavision.model.Venta;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author Jairo Ortega
 */
public interface VentaDAO {
     // Registrar una nueva venta
    boolean registrar(Venta venta);

  
    Optional<Venta> buscarPorId(int idVenta);

   
    List<Venta> listarTodas();

  
    List<Venta> listarPorCliente(int idCliente);

   
    List<Venta> listarPorVendedor(int idVendedor);

   
    boolean actualizarEstado(int idVenta, String estado);
}
