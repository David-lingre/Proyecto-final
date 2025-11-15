package com.granjapro.aplicacion.servicios;

import com.granjapro.dominio.modelos.Lote;
import com.granjapro.dominio.modelos.LogAuditoria;
import com.granjapro.dominio.modelos.RegistroProduccion;
import com.granjapro.dominio.modelos.Usuario;
import com.granjapro.dominio.repositorios.RepositorioAuditoria;
import com.granjapro.dominio.repositorios.RepositorioLote;
import com.granjapro.dominio.repositorios.RepositorioRegistroProduccion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de Producción.
 * 
 * Implementa la lógica de negocio relacionada con el registro y consulta
 * de producción de huevos. Utiliza inyección de dependencias por constructor
 * para interactuar con los repositorios.
 * 
 * Además, mantiene un registro de auditoría (RepositorioAuditoria) para
 * rastrear todos los cambios en registros de producción.
 */
public class ServicioProduccion {
    
    private RepositorioRegistroProduccion repositorioRegistroProduccion;
    private RepositorioLote repositorioLote;
    private RepositorioAuditoria repositorioAuditoria;
    
    /**
     * Constructor que recibe los repositorios por inyección de dependencias.
     *
     * @param repositorioRegistroProduccion la implementación del repositorio de registros de producción
     * @param repositorioLote la implementación del repositorio de lotes
     * @param repositorioAuditoria la implementación del repositorio de auditoría
     * @throws IllegalArgumentException si algún repositorio es nulo
     */
    public ServicioProduccion(RepositorioRegistroProduccion repositorioRegistroProduccion,
                              RepositorioLote repositorioLote,
                              RepositorioAuditoria repositorioAuditoria) {
        if (repositorioRegistroProduccion == null) {
            throw new IllegalArgumentException("El repositorio de registros no puede ser nulo");
        }
        if (repositorioLote == null) {
            throw new IllegalArgumentException("El repositorio de lotes no puede ser nulo");
        }
        if (repositorioAuditoria == null) {
            throw new IllegalArgumentException("El repositorio de auditoría no puede ser nulo");
        }
        
        this.repositorioRegistroProduccion = repositorioRegistroProduccion;
        this.repositorioLote = repositorioLote;
        this.repositorioAuditoria = repositorioAuditoria;
    }
    
    /**
     * Registra la producción de huevos de un lote para hoy.
     * 
     * Verifica que el lote exista, luego crea un registro de producción
     * con la fecha actual y los datos de huevos proporcionados.
     * La validación de que los huevos rotos no excedan los totales
     * se realiza en el setter del registro.
     *
     * @param idLote el identificador del lote
     * @param huevosTotales la cantidad total de huevos producidos
     * @param huevosRotos la cantidad de huevos rotos
     * @return el registro de producción creado y guardado (con ID asignado)
     * 
     * @throws IllegalArgumentException si los parámetros son inválidos o
     *         violan las validaciones del registro
     * @throws RuntimeException si el lote no existe
     */
    public RegistroProduccion registrarProduccion(String idLote, Integer huevosTotales, Integer huevosRotos) {
        // Validar parámetros de entrada
        if (idLote == null || idLote.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del lote no puede estar vacío");
        }
        if (huevosTotales == null || huevosTotales < 0) {
            throw new IllegalArgumentException("La cantidad de huevos totales no puede ser negativa");
        }
        if (huevosRotos == null || huevosRotos < 0) {
            throw new IllegalArgumentException("La cantidad de huevos rotos no puede ser negativa");
        }
        
        // Verificar que el lote existe
        Optional<Lote> loteOpt = repositorioLote.buscarPorId(idLote);
        if (!loteOpt.isPresent()) {
            throw new RuntimeException("No existe un lote con el ID: " + idLote);
        }
        
        // Crear el registro de producción con la fecha actual
        RegistroProduccion registro = new RegistroProduccion(
            null, // ID será asignado por la BD
            idLote,
            LocalDate.now(),
            huevosTotales,
            huevosRotos
        );
        
        // Guardar en el repositorio y retornar
        return repositorioRegistroProduccion.guardar(registro);
    }
    
    /**
     * Obtiene todos los registros de producción de un lote específico.
     * 
     * Útil para generar reportes y análisis de producción histórica
     * de un lote en particular.
     *
     * @param idLote el identificador del lote
     * @return una lista con todos los registros de producción del lote,
     *         puede estar vacía si no hay registros
     * 
     * @throws IllegalArgumentException si el ID del lote es nulo o vacío
     */
    public List<RegistroProduccion> obtenerRegistrosPorLote(String idLote) {
        if (idLote == null || idLote.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del lote no puede estar vacío");
        }
        
        return repositorioRegistroProduccion.buscarPorIdLote(idLote);
    }
    
    /**
     * Obtiene todos los registros de producción registrados en el sistema.
     *
     * @return una lista con todos los registros, puede estar vacía
     */
    public List<RegistroProduccion> listarTodosLosRegistros() {
        return repositorioRegistroProduccion.buscarTodos();
    }
    
    /**
     * Calcula el porcentaje de huevos rotos respecto al total para un lote.
     * 
     * Útil para análisis de calidad y eficiencia de producción.
     *
     * @param idLote el identificador del lote
     * @return el porcentaje de huevos rotos (0-100), o 0 si no hay registros
     * 
     * @throws IllegalArgumentException si el ID del lote es nulo o vacío
     */
    public double calcularPorcentajeRotos(String idLote) {
        if (idLote == null || idLote.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del lote no puede estar vacío");
        }
        
        List<RegistroProduccion> registros = repositorioRegistroProduccion.buscarPorIdLote(idLote);
        
        if (registros.isEmpty()) {
            return 0.0;
        }
        
        long totalHuevos = 0;
        long totalRotos = 0;
        
        for (RegistroProduccion registro : registros) {
            totalHuevos += registro.getHuevosTotales();
            totalRotos += registro.getHuevosRotos();
        }
        
        if (totalHuevos == 0) {
            return 0.0;
        }
        
        return (double) totalRotos / totalHuevos * 100;
    }
    
    // ═══════════════════════════════════════════════════════════
    // CORRECCIONES CON AUDITORÍA
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Corrige un registro de producción existente con trazabilidad de auditoría.
     * 
     * Cuando un Operario comete un error al registrar datos (ej: escribió 95 huevos
     * cuando fueron 105), este método permite corregir el registro dejando un
     * registro de auditoría que indica:
     * - Quién hizo el cambio (usuario/autor)
     * - Qué cambió (campo "huevosTotales" o "huevosRotos")
     * - Valores antes/después
     * - Por qué (motivo)
     * 
     * IMPORTANTE: Este método debe ser llamado solo por ADMIN.
     * La validación de roles NO se hace aquí (es responsabilidad de ConsolaUi/Servicio superior).
     * 
     * Ejemplo de uso:
     * <pre>
     *   Usuario admin = SesionGlobal.get().obtenerUsuario();
     *   servicioProduccion.corregirRegistroProduccion(
     *       "idRegistro123",
     *       105,  // Valor correcto de huevos
     *       -1,   // No cambiar huevosRotos (-1 = sin cambios)
     *       "El operario registró 95 cuando fueron 105",
     *       admin
     *   );
     * </pre>
     * 
     * @param idRegistro ID del registro a corregir
     * @param nuevosHuevosTotales nuevo valor de huevos totales (-1 para no cambiar)
     * @param nuevosHuevosRotos nuevo valor de huevos rotos (-1 para no cambiar)
     * @param motivo razón del cambio (para auditoría)
     * @param autor Usuario que hace la corrección (para auditoría)
     * 
     * @throws IllegalArgumentException si los parámetros son inválidos
     * @throws RuntimeException si el registro no existe
     */
    public void corregirRegistroProduccion(
            String idRegistro,
            Integer nuevosHuevosTotales,
            Integer nuevosHuevosRotos,
            String motivo,
            Usuario autor) {
        
        // Validaciones
        if (idRegistro == null || idRegistro.trim().isEmpty()) {
            throw new IllegalArgumentException("ID del registro no puede estar vacío");
        }
        if (motivo == null || motivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Motivo de la corrección no puede estar vacío");
        }
        if (autor == null) {
            throw new IllegalArgumentException("Usuario autor no puede ser null");
        }
        
        // Buscar el registro actual
        Optional<RegistroProduccion> registroActual = 
            repositorioRegistroProduccion.buscarPorId(idRegistro);
        
        if (!registroActual.isPresent()) {
            throw new RuntimeException("No existe registro de producción con ID: " + idRegistro);
        }
        
        RegistroProduccion registro = registroActual.get();
        
        // ═══════════════════════════════════════════════════════════
        // CORREGIR HUEVOS TOTALES
        // ═══════════════════════════════════════════════════════════
        
        if (nuevosHuevosTotales != null && nuevosHuevosTotales >= 0) {
            int valorAnterior = registro.getHuevosTotales();
            
            if (valorAnterior != nuevosHuevosTotales) {
                // Guardar auditoría
                LogAuditoria auditoria = new LogAuditoria(
                    autor.getId(),
                    autor.getNombre(),
                    idRegistro,
                    "PRODUCCION",
                    "huevosTotales",
                    String.valueOf(valorAnterior),
                    String.valueOf(nuevosHuevosTotales),
                    motivo
                );
                repositorioAuditoria.guardar(auditoria);
                
                // Actualizar valor
                registro.setHuevosTotales(nuevosHuevosTotales);
            }
        }
        
        // ═══════════════════════════════════════════════════════════
        // CORREGIR HUEVOS ROTOS
        // ═══════════════════════════════════════════════════════════
        
        if (nuevosHuevosRotos != null && nuevosHuevosRotos >= 0) {
            int valorAnterior = registro.getHuevosRotos();
            
            if (valorAnterior != nuevosHuevosRotos) {
                // Guardar auditoría
                LogAuditoria auditoria = new LogAuditoria(
                    autor.getId(),
                    autor.getNombre(),
                    idRegistro,
                    "PRODUCCION",
                    "huevosRotos",
                    String.valueOf(valorAnterior),
                    String.valueOf(nuevosHuevosRotos),
                    motivo
                );
                repositorioAuditoria.guardar(auditoria);
                
                // Actualizar valor
                registro.setHuevosRotos(nuevosHuevosRotos);
            }
        }
        
        // Guardar el registro actualizado en MongoDB
        repositorioRegistroProduccion.guardar(registro);
    }
}
