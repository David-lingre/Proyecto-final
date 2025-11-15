package com.granjapro.dominio.modelos;

/**
 * Roles disponibles en el sistema GranjaPro.
 * 
 * ADMIN: Acceso completo (crear usuarios, gestión completa, reportes)
 * OPERARIO: Acceso limitado (crear lotes, registrar producción, ver datos básicos)
 * 
 * @author David
 * @version 1.0
 */
public enum Rol {
    ADMIN("Administrador", "Acceso completo al sistema"),
    OPERARIO("Operario", "Acceso a operaciones básicas de producción");
    
    private final String descripcion;
    private final String permisos;
    
    /**
     * Constructor del enum.
     * 
     * @param descripcion nombre descriptivo del rol
     * @param permisos descripción de permisos
     */
    Rol(String descripcion, String permisos) {
        this.descripcion = descripcion;
        this.permisos = permisos;
    }
    
    /**
     * Obtiene la descripción del rol.
     * 
     * @return descripción
     */
    public String getDescripcion() {
        return descripcion;
    }
    
    /**
     * Obtiene la descripción de permisos.
     * 
     * @return permisos
     */
    public String getPermisos() {
        return permisos;
    }
    
    /**
     * Comprueba si este rol es ADMIN.
     * 
     * @return true si es ADMIN
     */
    public boolean esAdmin() {
        return this == ADMIN;
    }
    
    /**
     * Comprueba si este rol es OPERARIO.
     * 
     * @return true si es OPERARIO
     */
    public boolean esOperario() {
        return this == OPERARIO;
    }
}
