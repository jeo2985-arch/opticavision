/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.opticavision.dao;
import com.opticavision.model.Medicion;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author Jairo Ortega
 */
public interface MedicionDAO {
    
    boolean registrar(Medicion medicion);

   
    Optional<Medicion> buscarPorId(int idMedicion);

    
    List<Medicion> listarPorCliente(int idCliente);

    
    List<Medicion> listarTodos();

  
    boolean actualizar(Medicion medicion);
}
