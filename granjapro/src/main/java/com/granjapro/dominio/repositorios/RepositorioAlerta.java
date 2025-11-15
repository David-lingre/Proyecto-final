package com.granjapro.dominio.repositorios;

import com.granjapro.dominio.modelos.Alerta;
import com.granjapro.dominio.modelos.Alerta.TipoAlerta;
import java.time.LocalDate;
import java.util.List;

/**
 * Interfaz de repositorio para persister y consultar alertas.
 * Define el contrato que debe cumplir cualquier implementación (MongoDB, SQL, etc).
 * 
 * Operaciones principales:
 * - Guardar nuevas alertas generadas
 * - Buscar alertas por lote, tipo, estado, fecha
 * - Resolver alertas
 * - Generar reportes sobre alertas del sistema
 */
public interface RepositorioAlerta {
    
    /**
     * Guarda una nueva alerta en el repositorio.
     * @param alerta Alerta a guardar
     */
    void guardar(Alerta alerta);
    
    /**
     * Busca todas las alertas pendientes de un lote específico.
     * Útil para mostrar al operario: "¿Qué alertas hay pendientes en mi lote?"
     * @param idLote Identificador del lote
     * @return Lista de alertas pendientes (vacía si no hay)
     */
    List<Alerta> buscarPendientesPorLote(String idLote);
    
    /**
     * Busca todas las alertas (pendientes y resueltas) de un lote.
     * @param idLote Identificador del lote
     * @return Lista de todas las alertas del lote
     */
    List<Alerta> buscarPorLote(String idLote);
    
    /**
     * Busca alertas de un tipo específico (CRITICA, ADVERTENCIA, INFO).
     * @param tipo Tipo de alerta a buscar
     * @return Lista de alertas del tipo indicado
     */
    List<Alerta> buscarPorTipo(TipoAlerta tipo);
    
    /**
     * Busca alertas generadas en un rango de fechas.
     * Útil para reportes: "¿Qué problemas tuvimos en la semana?"
     * @param desde Fecha inicial (inclusive)
     * @param hasta Fecha final (inclusive)
     * @return Lista de alertas en el rango
     */
    List<Alerta> buscarPorRangoFechas(LocalDate desde, LocalDate hasta);
    
    /**
     * Obtiene todas las alertas críticas pendientes en el sistema.
     * El director las verá en el dashboard de emergencias.
     * @return Lista de alertas críticas no resueltas
     */
    List<Alerta> buscarCriticasPendientes();
    
    /**
     * Marca una alerta como resuelta.
     * @param idAlerta Identificador de la alerta a resolver
     */
    void resolver(String idAlerta);
    
    /**
     * Cuenta el total de alertas pendientes.
     * @return Número de alertas no resueltas
     */
    long contarPendientes();
    
    /**
     * Cuenta alertas de un tipo específico.
     * @param tipo Tipo de alerta
     * @return Número de alertas de ese tipo
     */
    long contarPorTipo(TipoAlerta tipo);
}
