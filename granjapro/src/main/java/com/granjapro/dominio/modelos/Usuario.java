package com.granjapro.dominio.modelos;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

/**
 * Representa un usuario del sistema GranjaPro con rol y credenciales.
 * 
 * IMPORTANTE: La contraseña NUNCA se almacena en texto plano.
 * Siempre se usa SHA-256 para hashing.
 * 
 * Ejemplo:
 *   Usuario usuario = new Usuario("juan", "mipass123", Rol.OPERARIO);
 *   usuario.validarPassword("mipass123");  // true
 *   usuario.validarPassword("otrapass");   // false
 * 
 * @author David
 * @version 1.0
 */
public class Usuario {
    
    private String id;
    private String nombre;
    private String passwordHasheado;
    private Rol rol;
    private boolean activo;
    
    // ═══════════════════════════════════════════════════════════
    // CONSTRUCTORES
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Constructor vacío (necesario para desserialización MongoDB).
     */
    public Usuario() {
        this.activo = true;
    }
    
    /**
     * Constructor con parámetros principales.
     * 
     * @param nombre nombre de usuario único
     * @param passwordPlano contraseña en texto plano (se hashea automáticamente)
     * @param rol ADMIN o OPERARIO
     */
    public Usuario(String nombre, String passwordPlano, Rol rol) {
        this();
        setNombre(nombre);
        setPasswordPlano(passwordPlano);
        setRol(rol);
    }
    
    // ═══════════════════════════════════════════════════════════
    // GETTERS Y SETTERS
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Obtiene el ID del usuario (generado por MongoDB).
     */
    public String getId() {
        return id;
    }
    
    /**
     * Establece el ID del usuario.
     */
    public void setId(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID no puede estar vacío");
        }
        this.id = id;
    }
    
    /**
     * Obtiene el nombre de usuario.
     */
    public String getNombre() {
        return nombre;
    }
    
    /**
     * Establece el nombre de usuario.
     * Validaciones: No vacío, mínimo 3 caracteres.
     */
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Nombre de usuario no puede estar vacío");
        }
        if (nombre.length() < 3) {
            throw new IllegalArgumentException("Nombre debe tener al menos 3 caracteres");
        }
        this.nombre = nombre;
    }
    
    /**
     * Obtiene el hash SHA-256 de la contraseña.
     * NOTA: Esta es la forma segura. Los hashes se comparan, nunca se desencriptan.
     */
    public String getPasswordHasheado() {
        return passwordHasheado;
    }
    
    /**
     * Establece la contraseña hasheando el texto plano con SHA-256.
     * 
     * IMPORTANTE: Esta es la forma correcta de guardar contraseñas.
     * Conversión: "mipass123" → "9c9064c59f1ffa2b46701211ee3d302c7e31b06f..."
     * 
     * Validaciones: No vacía, mínimo 6 caracteres.
     * 
     * @param passwordPlano contraseña sin encriptar
     */
    public void setPasswordPlano(String passwordPlano) {
        if (passwordPlano == null || passwordPlano.isBlank()) {
            throw new IllegalArgumentException("Contraseña no puede estar vacía");
        }
        if (passwordPlano.length() < 6) {
            throw new IllegalArgumentException("Contraseña debe tener al menos 6 caracteres");
        }
        this.passwordHasheado = hashearPassword(passwordPlano);
    }
    
    /**
     * Obtiene el rol del usuario.
     */
    public Rol getRol() {
        return rol;
    }
    
    /**
     * Establece el rol del usuario (ADMIN o OPERARIO).
     */
    public void setRol(Rol rol) {
        if (rol == null) {
            throw new IllegalArgumentException("Rol no puede ser nulo");
        }
        this.rol = rol;
    }
    
    /**
     * Comprueba si el usuario está activo.
     */
    public boolean isActivo() {
        return activo;
    }
    
    /**
     * Establece si el usuario está activo o desactivado.
     */
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    // ═══════════════════════════════════════════════════════════
    // MÉTODOS DE SEGURIDAD
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Verifica si la contraseña proporcionada coincide con la hasheada.
     * 
     * Proceso:
     * 1. Hashea la contraseña ingresada con SHA-256
     * 2. Compara el hash resultante con el hash guardado
     * 3. Retorna true si coinciden, false en otro caso
     * 
     * Uso:
     *   if (usuario.validarPassword("mipass123")) {
     *       // Contraseña correcta, iniciar sesión
     *   }
     * 
     * @param passwordPlano contraseña en texto plano a verificar
     * @return true si coincide, false en caso contrario
     */
    public boolean validarPassword(String passwordPlano) {
        if (passwordPlano == null || passwordPlano.isBlank()) {
            return false;
        }
        String hashIngresado = hashearPassword(passwordPlano);
        return hashIngresado.equals(this.passwordHasheado);
    }
    
    /**
     * Hashea una contraseña usando SHA-256.
     * 
     * Algoritmo:
     * 1. Toma la contraseña en texto plano: "mipass123"
     * 2. Aplica SHA-256 (función de hash criptográfico de 256 bits)
     * 3. Convierte resultado a hexadecimal (64 caracteres)
     * 4. Retorna: "9c9064c59f1ffa2b46701211ee3d302c7e31b06f..."
     * 
     * PROPIEDAD: Determinista (mismo input = mismo output)
     * PROPIEDAD: Irreversible (imposible obtener input del output)
     * 
     * @param password contraseña en texto plano
     * @return hash hexadecimal de 64 caracteres (SHA-256)
     */
    private static String hashearPassword(String password) {
        try {
            // 1. Obtener instancia de SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            
            // 2. Hashear la contraseña (convierte string a bytes)
            byte[] messageDigest = md.digest(password.getBytes());
            
            // 3. Convertir bytes a hexadecimal (representación legible)
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                // %02x: formato hexadecimal con 2 dígitos (ej: "9c", "a3")
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al hashear password: SHA-256 no disponible", e);
        }
    }
    
    /**
     * Comprueba si este usuario tiene rol de ADMIN.
     * 
     * @return true si está logueado y es admin
     */
    public boolean esAdmin() {
        return this.rol != null && this.rol.esAdmin();
    }
    
    /**
     * Comprueba si este usuario tiene rol de OPERARIO.
     * 
     * @return true si está logueado y es operario
     */
    public boolean esOperario() {
        return this.rol != null && this.rol.esOperario();
    }
    
    // ═══════════════════════════════════════════════════════════
    // OBJECT METHODS
    // ═══════════════════════════════════════════════════════════
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Usuario{" +
                "nombre='" + nombre + '\'' +
                ", rol=" + rol +
                ", activo=" + activo +
                '}';
    }
}
