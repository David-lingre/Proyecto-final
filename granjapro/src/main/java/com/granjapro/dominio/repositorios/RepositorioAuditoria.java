package com.granjapro.dominio.repositorios;

import com.granjapro.dominio.modelos.LogAuditoria;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Interface para persistencia de registros de auditoría.
 * 
 * Define las operaciones CRUD para LogAuditoria:
 * - Guardar nuevos registros (por cada cambio)
 * - Buscar por usuario (qué cambios hizo fulano)
 * - Buscar por entidad (quién cambió este lote/producción)
 * - Buscar por rango de fechas (auditoría en período específico)
 * 
 * La implementación (RepositorioAuditoriaMongo) maneja la persistencia en MongoDB.
 * 
 * @author Sistema
 * @version 1.0
 */
public interface RepositorioAuditoria {
    
    /**
     * Guarda un nuevo registro de auditoría.
     * 
     * @param log el registro a guardar
     * @throws IllegalArgumentException si log es null
     */
    void guardar(LogAuditoria log);
    
    /**
     * Busca todos los registros de un usuario específico.
     * 
     * Útil para: "¿Qué cambios hizo Juan en el sistema?"
     * 
     * @param idUsuario ID del usuario que hizo los cambios
     * @return lista de LogAuditoria (vacía si no hay registros)
     */
    List<LogAuditoria> buscarPorUsuario(String idUsuario);
    
    /**
     * Busca todos los cambios a una entidad específica.
     * 
     * Útil para: "¿Quién modificó el Lote L-001?"
     * 
     * @param idEntidad ID de la entidad (lote, registro producción, usuario)
     * @return lista de LogAuditoria (vacía si no hay registros)
     */
    List<LogAuditoria> buscarPorEntidad(String idEntidad);
    
    /**
     * Busca registros en un rango de fechas.
     * 
     * Útil para: "¿Qué cambios hubo en los últimos 7 días?"
     * 
     * @param desde fecha inicial (inclusiva)
     * @param hasta fecha final (inclusiva)
     * @return lista de LogAuditoria
     */
    List<LogAuditoria> buscarPorRangoFechas(LocalDateTime desde, LocalDateTime hasta);
    
    /**
     * Busca registros por tipo de entidad.
     * 
     * Útil para: "¿Quién modificó registros de PRODUCCION?"
     * 
     * @param tipoEntidad tipo de entidad (LOTE, PRODUCCION, USUARIO)
     * @return lista de LogAuditoria
     */
    List<LogAuditoria> buscarPorTipo(String tipoEntidad);
    
    /**
     * Obtiene todos los registros de auditoría (cuidado: puede ser mucho).
     * 
     * @return lista completa de LogAuditoria
     */
    List<LogAuditoria> obtenerTodos();
    
    /**
     * Cuenta cuántos cambios se hicieron a una entidad.
     * 
     * @param idEntidad ID de la entidad
     * @return número de cambios registrados
     */
    long contar(String idEntidad);
}
