package com.granjapro.dominio.modelos;

import java.time.LocalDate;

/**
 * Entidad de dominio que representa el registro diario de producción de un lote.
 * Almacena información sobre huevos producidos que se quebraron en un día específico.
 */
public class RegistroProduccion {
    
    /**
     * Identificador único del registro
     */
    private String id;
    
    /**
     * Identificador del lote al que corresponde este registro
     */
    private String idLote;
    
    /**
     * Fecha del registro de producción
     */
    private LocalDate fecha;
    
    /**
     * Cantidad total de huevos producidos en el día
     */
    private Integer huevosTotales;
    
    /**
     * Cantidad de huevos rotos o dañados en el día
     */
    private Integer huevosRotos;
    
    /**
     * Constructor vacío (requerido para Java Bean)
     */
    public RegistroProduccion() {
    }
    
    /**
     * Constructor completo con todos los parámetros.
     *
     * @param id identificador del registro
     * @param idLote identificador del lote
     * @param fecha fecha del registro
     * @param huevosTotales cantidad total de huevos producidos
     * @param huevosRotos cantidad de huevos rotos
     */
    public RegistroProduccion(String id, String idLote, LocalDate fecha, 
                              Integer huevosTotales, Integer huevosRotos) {
        this.id = id;
        this.idLote = idLote;
        this.fecha = fecha;
        this.huevosTotales = huevosTotales;
        this.huevosRotos = huevosRotos;
    }
    
    /**
     * Obtiene el identificador único del registro.
     *
     * @return el id del registro
     */
    public String getId() {
        return id;
    }
    
    /**
     * Establece el identificador único del registro.
     *
     * @param id el identificador a establecer
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Obtiene el identificador del lote.
     *
     * @return el identificador del lote
     */
    public String getIdLote() {
        return idLote;
    }
    
    /**
     * Establece el identificador del lote.
     *
     * @param idLote el identificador del lote a establecer
     */
    public void setIdLote(String idLote) {
        this.idLote = idLote;
    }
    
    /**
     * Obtiene la fecha del registro.
     *
     * @return la fecha del registro
     */
    public LocalDate getFecha() {
        return fecha;
    }
    
    /**
     * Establece la fecha del registro.
     *
     * @param fecha la fecha a establecer
     */
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    
    /**
     * Obtiene la cantidad total de huevos producidos.
     *
     * @return la cantidad total de huevos
     */
    public Integer getHuevosTotales() {
        return huevosTotales;
    }
    
    /**
     * Establece la cantidad total de huevos producidos.
     * Valida que no sea negativa y sea consistente con huevos rotos.
     *
     * @param huevosTotales la cantidad total de huevos a establecer
     * @throws IllegalArgumentException si la cantidad es negativa o menor que huevos rotos
     */
    public void setHuevosTotales(Integer huevosTotales) {
        if (huevosTotales != null && huevosTotales < 0) {
            throw new IllegalArgumentException("La cantidad de huevos totales no puede ser negativa");
        }
        if (huevosTotales != null && huevosRotos != null && huevosTotales < huevosRotos) {
            throw new IllegalArgumentException("Los huevos totales no pueden ser menores que los huevos rotos");
        }
        this.huevosTotales = huevosTotales;
    }
    
    /**
     * Obtiene la cantidad de huevos rotos.
     *
     * @return la cantidad de huevos rotos
     */
    public Integer getHuevosRotos() {
        return huevosRotos;
    }
    
    /**
     * Establece la cantidad de huevos rotos.
     * Valida que no sea negativa ni mayor que el total de huevos.
     *
     * @param huevosRotos la cantidad de huevos rotos a establecer
     * @throws IllegalArgumentException si la cantidad es negativa o mayor que el total
     */
    public void setHuevosRotos(Integer huevosRotos) {
        if (huevosRotos != null && huevosRotos < 0) {
            throw new IllegalArgumentException("La cantidad de huevos rotos no puede ser negativa");
        }
        if (huevosRotos != null && huevosTotales != null && huevosRotos > huevosTotales) {
            throw new IllegalArgumentException("Los huevos rotos no pueden ser mayores que los huevos totales");
        }
        this.huevosRotos = huevosRotos;
    }
    
    /**
     * Retorna una representación en texto del Registro de Producción.
     *
     * @return una cadena con información del registro
     */
    @Override
    public String toString() {
        return "RegistroProduccion{" +
                "id='" + id + '\'' +
                ", idLote='" + idLote + '\'' +
                ", fecha=" + fecha +
                ", huevosTotales=" + huevosTotales +
                ", huevosRotos=" + huevosRotos +
                '}';
    }
}
