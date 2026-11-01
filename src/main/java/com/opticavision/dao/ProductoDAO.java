/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.opticavision.dao;
import com.opticavision.model.Producto;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author Jairo Ortega
 */
public interface ProductoDAO {
   
    boolean registrar(Producto producto);

 
    Optional<Producto> buscarPorId(int idProducto);

   
    List<Producto> listarTodos();

   
    List<Producto> listarActivos();

   
    List<Producto> listarPorCategoria(String categoria);

   
    List<Producto> listarStockBajo();

    
    boolean actualizar(Producto producto);
}
