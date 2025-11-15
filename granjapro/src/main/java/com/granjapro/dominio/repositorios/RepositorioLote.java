package com.granjapro.dominio.repositorios;

import com.granjapro.dominio.modelos.Lote;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz de repositorio para la entidad Lote.
 * Define el contrato que cualquier implementación de persistencia debe cumplir.
 * El dominio no conoce los detalles de implementación (ej: MongoDB, SQL, etc).
 */
public interface RepositorioLote {
    
    /**
     * Guarda un nuevo lote o actualiza uno existente.
     *
     * @param lote el lote a guardar
     * @return el lote guardado (con ID asignado si es nuevo)
     */
    Lote guardar(Lote lote);
    
    /**
     * Busca un lote por su identificador único.
     *
     * @param id el identificador del lote
     * @return un Optional contiene el lote si existe, vacío si no
     */
    Optional<Lote> buscarPorId(String id);
    
    /**
     * Obtiene todos los lotes registrados en el sistema.
     *
     * @return una lista con todos los lotes
     */
    List<Lote> buscarTodos();
}
