package com.granjapro.dominio.excepciones;

/**
 * Excepción base para el dominio de GranjaPro.
 * 
 * Todas las excepciones personalizadas del dominio deben extender de esta clase.
 * Proporciona un punto centralizado para la jerarquía de excepciones semánticas.
 * 
 * @author GranjaPro Team
 * @version 1.0
 */
public abstract class GranjaException extends RuntimeException {
    
    /**
     * Constructor con mensaje de error.
     *
     * @param mensaje el mensaje descriptivo del error
     */
    public GranjaException(String mensaje) {
        super(mensaje);
    }
    
    /**
     * Constructor con mensaje y causa raíz.
     *
     * @param mensaje el mensaje descriptivo del error
     * @param causa la excepción que causó este error
     */
    public GranjaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
