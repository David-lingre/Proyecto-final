package com.granjapro.dominio.repositorios;

import com.granjapro.dominio.modelos.RegistroProduccion;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz de repositorio para la entidad RegistroProduccion.
 * Define el contrato que cualquier implementación de persistencia debe cumplir.
 * El dominio no conoce los detalles de implementación (ej: MongoDB, SQL, etc).
 */
public interface RepositorioRegistroProduccion {
    
    /**
     * Guarda un nuevo registro de producción o actualiza uno existente.
     *
     * @param registroProduccion el registro a guardar
     * @return el registro guardado (con ID asignado si es nuevo)
     */
    RegistroProduccion guardar(RegistroProduccion registroProduccion);
    
    /**
     * Busca un registro por su identificador único.
     *
     * @param id el identificador del registro
     * @return un Optional contiene el registro si existe, vacío si no
     */
    Optional<RegistroProduccion> buscarPorId(String id);
    
    /**
     * Obtiene todos los registros de producción registrados en el sistema.
     *
     * @return una lista con todos los registros
     */
    List<RegistroProduccion> buscarTodos();
    
    /**
     * Busca todos los registros de producción de un lote específico.
     *
     * @param idLote el identificador del lote
     * @return una lista con los registros del lote
     */
    List<RegistroProduccion> buscarPorIdLote(String idLote);
}
