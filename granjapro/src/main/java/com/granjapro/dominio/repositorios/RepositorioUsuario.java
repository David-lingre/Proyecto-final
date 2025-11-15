package com.granjapro.dominio.repositorios;

import com.granjapro.dominio.modelos.Usuario;
import java.util.List;

/**
 * Interfaz de repositorio para Usuario.
 * 
 * Define las operaciones de persistencia que TODO repositorio de Usuario
 * debe implementar, independientemente de la tecnología (MongoDB, SQL, etc).
 * 
 * Implementación: RepositorioUsuarioMongo
 * 
 * @author David
 * @version 1.0
 */
public interface RepositorioUsuario {
    
    /**
     * Busca un usuario por su nombre único.
     * 
     * @param nombre nombre del usuario a buscar
     * @return Usuario encontrado, null si no existe
     */
    Usuario buscarPorNombre(String nombre);
    
    /**
     * Busca un usuario por su ID.
     * 
     * @param id ID del usuario (MongoDB ObjectId)
     * @return Usuario encontrado, null si no existe
     */
    Usuario buscarPorId(String id);
    
    /**
     * Guarda un nuevo usuario en la base de datos.
     * 
     * @param usuario usuario a guardar (sin ID, se genera al guardar)
     */
    void guardar(Usuario usuario);
    
    /**
     * Actualiza un usuario existente.
     * 
     * @param usuario usuario con cambios (debe tener ID)
     */
    void actualizar(Usuario usuario);
    
    /**
     * Elimina un usuario.
     * 
     * @param id ID del usuario a eliminar
     */
    void eliminar(String id);
    
    /**
     * Obtiene todos los usuarios del sistema.
     * 
     * @return lista de todos los usuarios
     */
    List<Usuario> obtenerTodos();
    
    /**
     * Comprueba si existe un usuario con ese nombre.
     * 
     * @param nombre nombre a verificar
     * @return true si existe
     */
    boolean existePorNombre(String nombre);
}
