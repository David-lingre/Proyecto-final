package com.granjapro.dominio.modelos;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad que representa una alerta generada por el sistema de análisis automático.
 * Registro de problemas detectados en la producción de huevos.
 * 
 * Tipos de alerta:
 * - CRITICA: Problema grave que requiere atención inmediata
 * - ADVERTENCIA: Situación anómala pero controlable
 * - INFO: Evento informativo (ej: mortalidad registrada)
 * 
 * Estados:
 * - PENDIENTE: Alerta generada, aún sin resolver
 * - RESUELTA: Alerta revisada y cerrada por un operario
 */
public class Alerta {
    
    private String id;
    private LocalDate fecha;
    private String idLote;
    private TipoAlerta tipo;
    private String mensaje;
    private EstadoAlerta estado;
    
    /**
     * Constructor vacío para deserialización.
     */
    public Alerta() {
    }
    
    /**
     * Constructor para crear una alerta al momento de generarla.
     * @param idLote Identificador del lote afectado
     * @param tipo Tipo de alerta (CRITICA, ADVERTENCIA, INFO)
     * @param mensaje Descripción del problema detectado
     */
    public Alerta(String idLote, TipoAlerta tipo, String mensaje) {
        this.idLote = idLote;
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.fecha = LocalDate.now();
        this.estado = EstadoAlerta.PENDIENTE;
    }
    
    /**
     * Constructor completo para reconstruir desde persistencia.
     */
    public Alerta(String id, LocalDate fecha, String idLote, TipoAlerta tipo, 
                  String mensaje, EstadoAlerta estado) {
        this.id = id;
        this.fecha = fecha;
        this.idLote = idLote;
        this.tipo = tipo;
        this.mensaje = mensaje;
        this.estado = estado;
    }
    
    // ===================== GETTERS Y SETTERS =====================
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public LocalDate getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("La fecha de alerta no puede ser nula");
        }
        this.fecha = fecha;
    }
    
    public String getIdLote() {
        return idLote;
    }
    
    public void setIdLote(String idLote) {
        if (idLote == null || idLote.isBlank()) {
            throw new IllegalArgumentException("El ID del lote no puede estar vacío");
        }
        this.idLote = idLote;
    }
    
    public TipoAlerta getTipo() {
        return tipo;
    }
    
    public void setTipo(TipoAlerta tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de alerta no puede ser nulo");
        }
        this.tipo = tipo;
    }
    
    public String getMensaje() {
        return mensaje;
    }
    
    public void setMensaje(String mensaje) {
        if (mensaje == null || mensaje.isBlank()) {
            throw new IllegalArgumentException("El mensaje de alerta no puede estar vacío");
        }
        this.mensaje = mensaje;
    }
    
    public EstadoAlerta getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoAlerta estado) {
        if (estado == null) {
            throw new IllegalArgumentException("El estado de alerta no puede ser nulo");
        }
        this.estado = estado;
    }
    
    // ===================== MÉTODOS DE NEGOCIO =====================
    
    /**
     * Marca la alerta como resuelta.
     */
    public void resolver() {
        this.estado = EstadoAlerta.RESUELTA;
    }
    
    /**
     * Verifica si la alerta está pendiente.
     */
    public boolean estaPendiente() {
        return this.estado == EstadoAlerta.PENDIENTE;
    }
    
    /**
     * Verifica si la alerta es crítica.
     */
    public boolean esCritica() {
        return this.tipo == TipoAlerta.CRITICA;
    }
    
    // ===================== EQUALS, HASHCODE, TOSTRING =====================
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Alerta alerta = (Alerta) o;
        return Objects.equals(id, alerta.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Alerta{" +
                "id='" + id + '\'' +
                ", fecha=" + fecha +
                ", idLote='" + idLote + '\'' +
                ", tipo=" + tipo +
                ", mensaje='" + mensaje + '\'' +
                ", estado=" + estado +
                '}';
    }
    
    // ===================== ENUMS =====================
    
    /**
     * Tipos de alerta disponibles en el sistema.
     */
    public enum TipoAlerta {
        CRITICA("Crítica - Requiere atención inmediata"),
        ADVERTENCIA("Advertencia - Situación anómala"),
        INFO("Información - Evento registrado");
        
        private final String descripcion;
        
        TipoAlerta(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
    
    /**
     * Estados posibles de una alerta.
     */
    public enum EstadoAlerta {
        PENDIENTE("Alerta generada, sin resolver"),
        RESUELTA("Alerta revisada y cerrada");
        
        private final String descripcion;
        
        EstadoAlerta(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
}
