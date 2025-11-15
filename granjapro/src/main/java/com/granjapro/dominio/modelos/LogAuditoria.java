package com.granjapro.dominio.modelos;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registro de auditoría para trazabilidad de cambios en el sistema.
 * 
 * Cada vez que un usuario modifica un dato crítico (producción, usuarios, etc),
 * se crea un LogAuditoria con:
 * - Quién hizo el cambio (idUsuario)
 * - Qué cambió (idEntidadAfectada, campoModificado)
 * - Valores antes/después (valorAnterior, valorNuevo)
 * - Por qué (motivo)
 * - Cuándo (fecha)
 * 
 * Esto permite:
 * 1. Accountability: Saber quién cambió qué
 * 2. Reversibilidad: Restaurar valores previos si es necesario
 * 3. Compliance: Cumplir regulaciones de trazabilidad
 * 
 * @author Sistema
 * @version 1.0
 */
public class LogAuditoria {
    
    private String id;
    private LocalDateTime fecha;
    private String idUsuario;
    private String nombreUsuario;  // Para referencia sin ir a BD
    private String idEntidadAfectada;
    private String tipoEntidad;  // "LOTE", "PRODUCCION", "USUARIO", etc
    private String campoModificado;
    private String valorAnterior;
    private String valorNuevo;
    private String motivo;
    private String accion;  // "CREATE", "UPDATE", "DELETE"
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTORES
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Constructor vacío (para desserialización).
     */
    public LogAuditoria() {
        this.fecha = LocalDateTime.now();
    }
    
    /**
     * Constructor para UPDATE (cambio de valor).
     */
    public LogAuditoria(
            String idUsuario,
            String nombreUsuario,
            String idEntidadAfectada,
            String tipoEntidad,
            String campoModificado,
            String valorAnterior,
            String valorNuevo,
            String motivo) {
        this();
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.idEntidadAfectada = idEntidadAfectada;
        this.tipoEntidad = tipoEntidad;
        this.campoModificado = campoModificado;
        this.valorAnterior = valorAnterior;
        this.valorNuevo = valorNuevo;
        this.motivo = motivo;
        this.accion = "UPDATE";
    }
    
    /**
     * Constructor para CREATE o DELETE.
     */
    public LogAuditoria(
            String idUsuario,
            String nombreUsuario,
            String idEntidadAfectada,
            String tipoEntidad,
            String accion,
            String motivo) {
        this();
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.idEntidadAfectada = idEntidadAfectada;
        this.tipoEntidad = tipoEntidad;
        this.accion = accion;
        this.motivo = motivo;
    }
    
    // ═══════════════════════════════════════════════════════════
    // GETTERS Y SETTERS (Sin validación compleja, es registro histórico)
    // ═══════════════════════════════════════════════════════════
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public LocalDateTime getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDateTime fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("Fecha no puede ser null");
        }
        this.fecha = fecha;
    }
    
    public String getIdUsuario() {
        return idUsuario;
    }
    
    public void setIdUsuario(String idUsuario) {
        if (idUsuario == null || idUsuario.isBlank()) {
            throw new IllegalArgumentException("ID usuario no puede estar vacío");
        }
        this.idUsuario = idUsuario;
    }
    
    public String getNombreUsuario() {
        return nombreUsuario;
    }
    
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
    
    public String getIdEntidadAfectada() {
        return idEntidadAfectada;
    }
    
    public void setIdEntidadAfectada(String idEntidadAfectada) {
        if (idEntidadAfectada == null || idEntidadAfectada.isBlank()) {
            throw new IllegalArgumentException("ID entidad afectada no puede estar vacío");
        }
        this.idEntidadAfectada = idEntidadAfectada;
    }
    
    public String getTipoEntidad() {
        return tipoEntidad;
    }
    
    public void setTipoEntidad(String tipoEntidad) {
        if (tipoEntidad == null || tipoEntidad.isBlank()) {
            throw new IllegalArgumentException("Tipo entidad no puede estar vacío");
        }
        this.tipoEntidad = tipoEntidad;
    }
    
    public String getCampoModificado() {
        return campoModificado;
    }
    
    public void setCampoModificado(String campoModificado) {
        this.campoModificado = campoModificado;
    }
    
    public String getValorAnterior() {
        return valorAnterior;
    }
    
    public void setValorAnterior(String valorAnterior) {
        this.valorAnterior = valorAnterior;
    }
    
    public String getValorNuevo() {
        return valorNuevo;
    }
    
    public void setValorNuevo(String valorNuevo) {
        this.valorNuevo = valorNuevo;
    }
    
    public String getMotivo() {
        return motivo;
    }
    
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
    
    public String getAccion() {
        return accion;
    }
    
    public void setAccion(String accion) {
        if (accion == null || (!accion.equals("CREATE") && 
                              !accion.equals("UPDATE") && 
                              !accion.equals("DELETE"))) {
            throw new IllegalArgumentException(
                "Acción debe ser CREATE, UPDATE o DELETE. Recibiste: " + accion
            );
        }
        this.accion = accion;
    }
    
    // ═══════════════════════════════════════════════════════════
    // MÉTODOS ÚTILES
    // ═══════════════════════════════════════════════════════════
    
    @Override
    public String toString() {
        return "LogAuditoria{" +
                "id='" + id + '\'' +
                ", fecha=" + fecha +
                ", usuario='" + nombreUsuario + '\'' +
                ", tipo='" + tipoEntidad + '\'' +
                ", accion='" + accion + '\'' +
                ", campo='" + campoModificado + '\'' +
                ", anterior='" + valorAnterior + '\'' +
                ", nuevo='" + valorNuevo + '\'' +
                ", motivo='" + motivo + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogAuditoria that = (LogAuditoria) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
