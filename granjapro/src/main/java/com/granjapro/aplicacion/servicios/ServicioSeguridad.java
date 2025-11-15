package com.granjapro.aplicacion.servicios;

import com.granjapro.dominio.modelos.Usuario;
import com.granjapro.dominio.modelos.Rol;
import com.granjapro.dominio.repositorios.RepositorioUsuario;
import com.granjapro.aplicacion.sesion.SesionGlobal;

/**
 * Servicio de Autenticación y Autorización.
 * 
 * Responsabilidades principales:
 * 1. AUTENTICACIÓN: Verificar identidad (usuario + password)
 * 2. AUTORIZACIÓN: Verificar permisos según roles
 * 3. GESTIÓN DE SESIÓN: Iniciar/cerrar sesiones
 * 4. GESTIÓN DE USUARIOS: Crear, actualizar, listar usuarios (solo admin)
 * 
 * Lógica de seguridad:
 * - Las contraseñas se comparan usando SHA-256
 * - Mensajes de error genéricos (no revelar qué falló específicamente)
 * - Solo ADMIN puede crear usuarios
 * - Sesión guardada en Singleton SesionGlobal
 * 
 * @author David
 * @version 1.0
 */
public class ServicioSeguridad {
    
    private RepositorioUsuario repositorioUsuario;
    
    /**
     * Constructor con inyección de dependencia.
     * 
     * El RepositorioUsuario se proporciona desde afuera (en ConsolaUi)
     * permitiendo diferentes implementaciones (MongoDB, SQL, etc).
     * 
     * @param repositorioUsuario implementación del repositorio de usuarios
     * @throws IllegalArgumentException si repositorio es nulo
     */
    public ServicioSeguridad(RepositorioUsuario repositorioUsuario) {
        if (repositorioUsuario == null) {
            throw new IllegalArgumentException("Repositorio de usuario no puede ser nulo");
        }
        this.repositorioUsuario = repositorioUsuario;
    }
    
    // ═══════════════════════════════════════════════════════════
    // AUTENTICACIÓN
    // ═══════════════════════════════════════════════════════════
    
    /**
     * MÉTODO CRÍTICO: Realiza autenticación (login).
     * 
     * ALGORITMO:
     * 1. Valida que entrada no esté vacía
     * 2. Busca usuario en base de datos por nombre
     * 3. Si no existe, lanza excepción genérica (seguridad)
     * 4. Verifica que usuario esté activo
     * 5. Valida contraseña (SHA-256)
     * 6. Si todo es válido, inicia sesión en SesionGlobal
     * 7. Retorna Usuario autenticado
     * 
     * SEGURIDAD: 
     * - Mensajes genéricos ("Credenciales inválidas")
     * - No revela si existe el usuario o qué falló
     * - Compara hashes, nunca texto plano
     * 
     * EJEMPLO DE USO (en ConsolaUi.mostrarSplashYLogin()):
     * 
     *   try {
     *       System.out.print("Usuario: ");
     *       String usuario = scanner.nextLine();
     *       System.out.print("Contraseña: ");
     *       String password = scanner.nextLine();
     *       
     *       Usuario usuarioAutenticado = servicioSeguridad.login(usuario, password);
     *       System.out.println("✅ Bienvenido " + usuarioAutenticado.getNombre());
     *       
     *   } catch (Exception e) {
     *       System.out.println("❌ Acceso denegado: " + e.getMessage());
     *   }
     * 
     * @param nombreUsuario nombre de usuario
     * @param passwordPlano contraseña en texto plano
     * @return Usuario autenticado y logueado en SesionGlobal
     * @throws Exception si credenciales son inválidas
     */
    public Usuario login(String nombreUsuario, String passwordPlano) throws Exception {
        
        // 1. Validar entrada
        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            throw new Exception("Usuario no puede estar vacío");
        }
        if (passwordPlano == null || passwordPlano.isBlank()) {
            throw new Exception("Contraseña no puede estar vacía");
        }
        
        // 2. Buscar usuario en base de datos
        Usuario usuario = repositorioUsuario.buscarPorNombre(nombreUsuario);
        if (usuario == null) {
            // SEGURIDAD: Mensaje genérico (no revelar si existe o no)
            throw new Exception("Credenciales inválidas");
        }
        
        // 3. Verificar que usuario está activo
        if (!usuario.isActivo()) {
            throw new Exception("Usuario inactivo. Contacta al administrador");
        }
        
        // 4. Validar contraseña (SHA-256)
        // El método validarPassword() hasea la entrada y compara con lo guardado
        if (!usuario.validarPassword(passwordPlano)) {
            // SEGURIDAD: Mensaje genérico (no revelar qué falló específicamente)
            throw new Exception("Credenciales inválidas");
        }
        
        // 5. Login exitoso: iniciar sesión en SesionGlobal
        SesionGlobal.get().iniciarSesion(usuario);
        
        return usuario;
    }
    
    /**
     * Cierra la sesión actual (logout).
     * 
     * Limpia SesionGlobal, dejando la app lista para nuevo login.
     * 
     * Uso (en ConsolaUi.cerrarSesion()):
     *   servicioSeguridad.logout();
     *   mostrarSplashYLogin();  // Vuelve a mostrar pantalla de login
     */
    public void logout() {
        SesionGlobal.get().cerrarSesion();
    }
    
    // ═══════════════════════════════════════════════════════════
    // AUTORIZACIÓN
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Verifica si el usuario actual es ADMIN.
     * 
     * Uso en ConsolaUi (ocultar/mostrar opciones):
     *   if (servicioSeguridad.esAdmin()) {
     *       System.out.println("║  3️⃣  Gestionar Usuarios     ║");
     *   }
     * 
     * @return true si está logueado y es admin
     */
    public boolean esAdmin() {
        return SesionGlobal.get().esAdmin();
    }
    
    /**
     * Verifica si el usuario actual es OPERARIO.
     * 
     * @return true si está logueado y es operario
     */
    public boolean esOperario() {
        return SesionGlobal.get().esOperario();
    }
    
    /**
     * Verifica si hay usuario logueado.
     * 
     * @return true si hay sesión activa
     */
    public boolean estaLogueado() {
        return SesionGlobal.get().estaLogueado();
    }
    
    // ═══════════════════════════════════════════════════════════
    // GESTIÓN DE USUARIOS (SOLO ADMIN)
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Crea un nuevo usuario en el sistema.
     * 
     * RESTRICCIÓN: Solo ADMIN puede crear usuarios.
     * 
     * VALIDACIONES:
     * - Verificar que quien lo solicita es ADMIN
     * - Verificar que usuario no existe
     * - Nombre mínimo 3 caracteres
     * - Contraseña mínimo 6 caracteres
     * - Rol válido (ADMIN o OPERARIO)
     * 
     * EJEMPLO (en futuro menú admin):
     *   try {
     *       servicioSeguridad.crearUsuario("juan", "mipass123", "OPERARIO");
     *       System.out.println("✅ Usuario creado");
     *   } catch (Exception e) {
     *       System.out.println("❌ Error: " + e.getMessage());
     *   }
     * 
     * @param nombre nombre del nuevo usuario
     * @param passwordPlano contraseña en texto plano (se hashea automáticamente)
     * @param rol "ADMIN" o "OPERARIO"
     * @return Usuario creado
     * @throws Exception si no es admin o usuario ya existe
     */
    public Usuario crearUsuario(String nombre, String passwordPlano, String rol) 
            throws Exception {
        
        // 1. Verificar que quien lo solicita es ADMIN
        if (!esAdmin()) {
            throw new Exception("Solo administradores pueden crear usuarios");
        }
        
        // 2. Validaciones básicas
        if (nombre == null || nombre.isBlank()) {
            throw new Exception("Nombre no puede estar vacío");
        }
        if (nombre.length() < 3) {
            throw new Exception("Nombre debe tener al menos 3 caracteres");
        }
        
        // 3. Buscar si ya existe
        Usuario existente = repositorioUsuario.buscarPorNombre(nombre);
        if (existente != null) {
            throw new Exception("Usuario '" + nombre + "' ya existe");
        }
        
        // 4. Validar rol
        Rol rolEnum;
        try {
            rolEnum = Rol.valueOf(rol.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new Exception("Rol inválido. Debe ser ADMIN o OPERARIO");
        }
        
        // 5. Crear usuario (el constructor valida password)
        Usuario nuevoUsuario = new Usuario(nombre, passwordPlano, rolEnum);
        
        // 6. Guardar en base de datos
        try {
            repositorioUsuario.guardar(nuevoUsuario);
        } catch (Exception e) {
            throw new Exception("Error al guardar usuario: " + e.getMessage());
        }
        
        return nuevoUsuario;
    }
    
    /**
     * Desactiva un usuario (no lo elimina, lo marca como inactivo).
     * 
     * RESTRICCIÓN: Solo ADMIN.
     * 
     * @param idUsuario ID del usuario a desactivar
     * @throws Exception si no es admin o usuario no existe
     */
    public void desactivarUsuario(String idUsuario) throws Exception {
        if (!esAdmin()) {
            throw new Exception("Solo administradores pueden desactivar usuarios");
        }
        
        Usuario usuario = repositorioUsuario.buscarPorId(idUsuario);
        if (usuario == null) {
            throw new Exception("Usuario no encontrado");
        }
        
        usuario.setActivo(false);
        repositorioUsuario.actualizar(usuario);
    }
    
    /**
     * Obtiene todos los usuarios del sistema.
     * 
     * RESTRICCIÓN: Solo ADMIN.
     * 
     * @return lista de todos los usuarios
     * @throws Exception si no es admin
     */
    public java.util.List<Usuario> obtenerTodosLosUsuarios() throws Exception {
        if (!esAdmin()) {
            throw new Exception("Solo administradores pueden listar usuarios");
        }
        
        return repositorioUsuario.obtenerTodos();
    }
    
    /**
     * Obtiene información del usuario actual.
     * 
     * @return Usuario logueado o null si no hay sesión
     */
    public Usuario obtenerUsuarioActual() {
        return SesionGlobal.get().obtenerUsuario();
    }
}
