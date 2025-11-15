package com.granjapro.dominio.modelos;

import java.time.LocalDate;

/**

 * Un lote agrupa gallinas del mismo tipo, edad y características comunes.
 */
public class Lote {
    
    /**
     * Identificador único del lote
     */
    private String id;
    
    /**
     * Código único del lote
     */
    private String codigo;
    
    /**
     * Raza de las gallinas en el lote
     */
    private String raza;
    
    /**
     * Cantidad inicial de gallinas al crear el lote
     */
    private Integer cantidadInicial;
    
    /**
     * Cantidad actual de gallinas vivas en el lote
     */
    private Integer cantidadActual;
    
    /**
     * Fecha en la que se creó o ingresó el lote a la granja
     */
    private LocalDate fechaIngreso;
    
    /**
     * Identificador del corral donde se ubica el lote
     */
    private String idCorral;
    
    /**
     * Constructor vacío 
     */
    public Lote() {
    }
    
    /**
     * Constructor completo con todos los parámetros.
     *
     * @param id identificador del lote
     * @param codigo código único del lote
     * @param raza raza de las gallinas
     * @param cantidadInicial cantidad inicial de gallinas
     * @param cantidadActual cantidad actual de gallinas
     * @param fechaIngreso fecha de ingreso del lote
     * @param idCorral identificador del corral
     */
    public Lote(String id, String codigo, String raza, Integer cantidadInicial, 
                Integer cantidadActual, LocalDate fechaIngreso, String idCorral) {
        this.id = id;
        this.codigo = codigo;
        this.raza = raza;
        this.cantidadInicial = cantidadInicial;
        this.cantidadActual = cantidadActual;
        this.fechaIngreso = fechaIngreso;
        this.idCorral = idCorral;
    }
    
    /**
     * Obtiene el identificador único del lote.
     *
     * @return el id del lote
     */
    public String getId() {
        return id;
    }
    
    /**
     * Establece el identificador único del lote.
     *
     * @param id el identificador a establecer
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Obtiene el código del lote.
     *
     * @return el código del lote
     */
    public String getCodigo() {
        return codigo;
    }
    
    /**
     * Establece el código del lote.
     *
     * @param codigo el código a establecer
     */
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    
    /**
     * Obtiene la raza de las gallinas.
     *
     * @return la raza
     */
    public String getRaza() {
        return raza;
    }
    
    /**
     * Establece la raza de las gallinas.
     *
     * @param raza la raza a establecer
     */
    public void setRaza(String raza) {
        this.raza = raza;
    }
    
    /**
     * Obtiene la cantidad inicial de gallinas.
     *
     * @return la cantidad inicial
     */
    public Integer getCantidadInicial() {
        return cantidadInicial;
    }
    
    /**
     * Establece la cantidad inicial de gallinas.
     * Valida que no sea negativa.
     *
     * @param cantidadInicial la cantidad inicial a establecer
     * @throws IllegalArgumentException si la cantidad es negativa
     */
    public void setCantidadInicial(Integer cantidadInicial) {
        if (cantidadInicial != null && cantidadInicial < 0) {
            throw new IllegalArgumentException("La cantidad inicial no puede ser negativa");
        }
        this.cantidadInicial = cantidadInicial;
    }
    
    /**
     * Obtiene la cantidad actual de gallinas vivas.
     *
     * @return la cantidad actual
     */
    public Integer getCantidadActual() {
        return cantidadActual;
    }
    
    /**
     * Establece la cantidad actual de gallinas vivas.
     * Valida que no sea negativa ni mayor que la cantidad inicial.
     *
     * @param cantidadActual la cantidad actual a establecer
     * @throws IllegalArgumentException si la cantidad es negativa o mayor que la inicial
     */
    public void setCantidadActual(Integer cantidadActual) {
        if (cantidadActual != null && cantidadActual < 0) {
            throw new IllegalArgumentException("La cantidad actual no puede ser negativa");
        }
        if (cantidadActual != null && cantidadInicial != null && cantidadActual > cantidadInicial) {
            throw new IllegalArgumentException("La cantidad actual no puede ser mayor que la cantidad inicial");
        }
        this.cantidadActual = cantidadActual;
    }
    
    /**
     * Obtiene la fecha de ingreso del lote.
     *
     * @return la fecha de ingreso
     */
    public LocalDate getFechaIngreso() {
        return fechaIngreso;
    }
    
    /**
     * Establece la fecha de ingreso del lote.
     *
     * @param fechaIngreso la fecha de ingreso a establecer
     */
    public void setFechaIngreso(LocalDate fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }
    
    /**
     * Obtiene el identificador del corral.
     *
     * @return el identificador del corral
     */
    public String getIdCorral() {
        return idCorral;
    }
    
    /**
     * Establece el identificador del corral.
     *
     * @param idCorral el identificador del corral a establecer
     */
    public void setIdCorral(String idCorral) {
        this.idCorral = idCorral;
    }
    
    /**
     * Retorna una representación en texto del Lote.
     *
     * @return una cadena con información del lote
     */
    @Override
    public String toString() {
        return "Lote{" +
                "id='" + id + '\'' +
                ", codigo='" + codigo + '\'' +
                ", raza='" + raza + '\'' +
                ", cantidadInicial=" + cantidadInicial +
                ", cantidadActual=" + cantidadActual +
                ", fechaIngreso=" + fechaIngreso +
                ", idCorral='" + idCorral + '\'' +
                '}';
    }
}
