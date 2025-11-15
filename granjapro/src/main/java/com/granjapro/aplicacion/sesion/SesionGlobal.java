package com.granjapro.aplicacion.sesion;

import com.granjapro.dominio.modelos.Usuario;

/**
 * Singleton que mantiene la sesión del usuario logueado en memoria.
 * 
 * CARACTERÍSTICAS CLAVE:
 * - Solo hay UNA instancia de SesionGlobal en toda la aplicación
 * - Se crea al iniciar la app y vive mientras esté abierta
 * - Guarda al usuario logueado tras login exitoso
 * - Se limpia en logout
 * 
 * USO DESDE CUALQUIER PARTE DEL CÓDIGO:
 *   // Obtener usuario actual
 *   Usuario actual = SesionGlobal.get().obtenerUsuario();
 *   
 *   // Verificar si es admin
 *   if (SesionGlobal.get().esAdmin()) {
 *       // Mostrar opciones de admin
 *   }
 *   
 *   // Obtener nombre del usuario actual
 *   String nombre = SesionGlobal.get().obtenerNombreUsuario();
 * 
 * CICLO DE VIDA:
 * 1. App inicia → SesionGlobal existe sin usuario
 * 2. Usuario hace login → servicioSeguridad.login() → SesionGlobal.get().iniciarSesion()
 * 3. Usuario navega app → cualquier código accede a SesionGlobal.get()
 * 4. Usuario hace logout → servicioSeguridad.logout() → SesionGlobal.get().cerrarSesion()
 * 5. App cierra → SesionGlobal desaparece
 * 
 * @author David
 * @version 1.0
 */
public class SesionGlobal {
    
    // ═══════════════════════════════════════════════════════════
    // SINGLETON PATTERN
    // ═══════════════════════════════════════════════════════════
    
    /**
     * La única instancia de SesionGlobal en toda la aplicación.
     * Se inicializa lazy (cuando se accede por primera vez).
     */
    private static SesionGlobal instancia;
    
    /**
     * El usuario que actualmente está logueado (null si no hay sesión).
     */
    private Usuario usuarioLogueado;
    
    /**
     * Constructor privado (Singleton pattern).
     * 
     * No se puede hacer: new SesionGlobal()
     * 
     * Se accede solo mediante: SesionGlobal.get()
     */
    private SesionGlobal() {
        this.usuarioLogueado = null;
    }
    
    // ═══════════════════════════════════════════════════════════
    // ACCESO A LA INSTANCIA
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Obtiene la instancia única de SesionGlobal.
     * 
     * Si no existe la instancia, la crea (lazy initialization).
     * Luego, siempre retorna la misma instancia.
     * 
     * Uso: SesionGlobal.obtenerInstancia().esAdmin()
     * 
     * @return la instancia única de SesionGlobal
     */
    public static SesionGlobal obtenerInstancia() {
        if (instancia == null) {
            instancia = new SesionGlobal();
        }
        return instancia;
    }
    
    /**
     * ALIAS corto para obtenerInstancia().
     * Es la forma recomendada de acceso.
     * 
     * Uso: SesionGlobal.get().esAdmin()
     * 
     * @return la instancia única de SesionGlobal
     */
    public static SesionGlobal get() {
        return obtenerInstancia();
    }
    
    // ═══════════════════════════════════════════════════════════
    // GESTIÓN DE SESIÓN
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Inicia sesión con un usuario autenticado.
     * 
     * Este método se invoca desde ServicioSeguridad.login() después de
     * validar credenciales correctamente.
     * 
     * Uso:
     *   Usuario usuarioAutenticado = servicioSeguridad.login(user, pass);
     *   // Si llegó aquí, credenciales son válidas
     *   SesionGlobal.get().iniciarSesion(usuarioAutenticado);
     * 
     * @param usuario el usuario autenticado
     * @throws IllegalArgumentException si usuario es nulo o inactivo
     */
    public void iniciarSesion(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no puede ser nulo");
        }
        if (!usuario.isActivo()) {
            throw new IllegalArgumentException("Usuario inactivo");
        }
        this.usuarioLogueado = usuario;
    }
    
    /**
     * Obtiene el usuario logueado actualmente.
     * 
     * Uso:
     *   Usuario actual = SesionGlobal.get().obtenerUsuario();
     *   if (actual != null) {
     *       System.out.println("Hola " + actual.getNombre());
     *   }
     * 
     * @return Usuario logueado, null si no hay sesión
     */
    public Usuario obtenerUsuario() {
        return usuarioLogueado;
    }
    
    /**
     * Cierra la sesión actual (logout).
     * 
     * Limpia la referencia al usuario, dejando SesionGlobal vacía
     * (lista para un nuevo login).
     * 
     * Uso:
     *   SesionGlobal.get().cerrarSesion();
     *   // Ahora estaLogueado() = false
     */
    public void cerrarSesion() {
        this.usuarioLogueado = null;
    }
    
    // ═══════════════════════════════════════════════════════════
    // CONSULTAS DE SESIÓN
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Comprueba si hay usuario logueado actualmente.
     * 
     * Uso:
     *   if (SesionGlobal.get().estaLogueado()) {
     *       // Mostrar menú principal
     *   } else {
     *       // Mostrar pantalla de login
     *   }
     * 
     * @return true si hay sesión activa, false en caso contrario
     */
    public boolean estaLogueado() {
        return usuarioLogueado != null;
    }
    
    /**
     * Comprueba si el usuario logueado tiene rol ADMIN.
     * 
     * USO MUY IMPORTANTE en ConsolaUi para ocultar/mostrar opciones:
     * 
     *   if (SesionGlobal.get().esAdmin()) {
     *       System.out.println("║  3️⃣  Gestionar Usuarios     ║");
     *       System.out.println("║  4️⃣  Ver Reportes          ║");
     *   }
     * 
     * @return true si está logueado Y es admin, false en caso contrario
     */
    public boolean esAdmin() {
        return estaLogueado() && usuarioLogueado.esAdmin();
    }
    
    /**
     * Comprueba si el usuario logueado tiene rol OPERARIO.
     * 
     * @return true si está logueado Y es operario, false en caso contrario
     */
    public boolean esOperario() {
        return estaLogueado() && usuarioLogueado.esOperario();
    }
    
    /**
     * Obtiene el nombre del usuario logueado.
     * 
     * Uso:
     *   String nombre = SesionGlobal.get().obtenerNombreUsuario();
     *   System.out.println("Hola " + nombre);
     * 
     * @return nombre del usuario si está logueado, "No logueado" en otro caso
     */
    public String obtenerNombreUsuario() {
        if (estaLogueado()) {
            return usuarioLogueado.getNombre();
        }
        return "No logueado";
    }
    
    /**
     * Obtiene el rol del usuario logueado como String.
     * 
     * @return nombre del rol ("ADMIN", "OPERARIO") o "SIN SESIÓN"
     */
    public String obtenerRol() {
        if (estaLogueado()) {
            return usuarioLogueado.getRol().toString();
        }
        return "SIN SESIÓN";
    }
}
