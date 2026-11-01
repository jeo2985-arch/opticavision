/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.opticavision.dao;
import com.opticavision.model.Cliente;
import java.util.List;
import java.util.Optional;
/**
 *
 * @author Jairo Ortega
 */
public interface ClienteDAO {
   
    boolean registrar(Cliente cliente);

    
    Optional<Cliente> buscarPorId(int idCliente);

    
    Optional<Cliente> buscarPorDni(String dni);

    
    Optional<Cliente> buscarPorUsuario(int idUsuario);

   
    List<Cliente> listarTodos();

  
    boolean actualizar(Cliente cliente);
}
