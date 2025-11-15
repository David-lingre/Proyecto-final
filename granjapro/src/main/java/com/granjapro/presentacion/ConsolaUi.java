package com.granjapro.presentacion;

import com.granjapro.dominio.modelos.Lote;
import com.granjapro.dominio.modelos.RegistroProduccion;
import com.granjapro.dominio.modelos.Alerta;
import com.granjapro.dominio.excepciones.GranjaException;
import com.granjapro.aplicacion.servicios.ServicioGestionLotes;
import com.granjapro.aplicacion.servicios.ServicioProduccion;
import com.granjapro.aplicacion.servicios.ServicioAnalitica;

// 🔐 NUEVOS IMPORTS DE SEGURIDAD
import com.granjapro.aplicacion.servicios.ServicioSeguridad;
import com.granjapro.aplicacion.sesion.SesionGlobal;
import com.granjapro.dominio.modelos.Usuario;
import com.granjapro.dominio.repositorios.RepositorioUsuario;
import com.granjapro.infraestructura.persistencia.mongo.RepositorioUsuarioMongo;

import com.granjapro.infraestructura.persistencia.mongo.RepositorioLoteMongo;
import com.granjapro.infraestructura.persistencia.mongo.RepositorioRegistroProduccionMongo;
import com.granjapro.infraestructura.persistencia.mongo.RepositorioAuditoriaMongo;
import com.granjapro.infraestructura.persistencia.mongo.RepositorioAlertaMongo;
import com.granjapro.infraestructura.persistencia.mongo.ConexionMongo;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.List;
import java.util.Scanner;
import java.util.Optional;

/**
 * Interfaz de Usuario en Consola (CLI) para GranjaPro.
 * 
 * Proporciona una experiencia de usuario profesional en línea de comandos
 * con menú ASCII y manejo elegante de errores.
 * 
 * @author Equipo GranjaPro
 * @version 1.0
 */
public class ConsolaUi {
    
    // Códigos ANSI para colores en consola
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_CYAN = "\u001B[36m";
    
    private Scanner scanner;
    private ServicioGestionLotes servicioGestionLotes;
    private ServicioProduccion servicioProduccion;
    private ServicioAnalitica servicioAnalitica;

    // 🔐 NUEVO: servicio de seguridad
    private ServicioSeguridad servicioSeguridad;
    
    /**
     * Constructor que inicializa la interfaz con los servicios.
     */
    public ConsolaUi() {
        this.scanner = new Scanner(System.in);
        
        // Inicializar repositorios de MongoDB
        MongoDatabase db = ConexionMongo.obtenerInstancia().obtenerBaseDatos();
        
        // Servicios de Gestión
        this.servicioGestionLotes = new ServicioGestionLotes(new RepositorioLoteMongo());
        
        // Servicio de Producción con Auditoría
        MongoCollection<Document> coleccionAuditoria = db.getCollection("auditoria");
        this.servicioProduccion = new ServicioProduccion(
            new RepositorioRegistroProduccionMongo(),
            new RepositorioLoteMongo(),
            new RepositorioAuditoriaMongo(coleccionAuditoria)
        );
        
        // Servicio de Analítica con Alertas
        MongoCollection<Document> coleccionAlertas = db.getCollection("alertas");
        this.servicioAnalitica = new ServicioAnalitica(
            new RepositorioLoteMongo(),
            new RepositorioRegistroProduccionMongo(),
            new RepositorioAlertaMongo(coleccionAlertas)
        );

        // 🔐 NUEVO: inicializar seguridad (repositorio de usuarios + servicio)
        MongoCollection<Document> coleccionUsuarios = db.getCollection("usuarios");
        RepositorioUsuario repositorioUsuario = new RepositorioUsuarioMongo(coleccionUsuarios);
        this.servicioSeguridad = new ServicioSeguridad(repositorioUsuario);
    }
    
    /**
     * Inicia la aplicación y muestra el menú principal.
     */
    public void iniciar() {
        mostrarBienvenida();
        
        boolean salir = false;
        while (!salir) {
            mostrarMenuPrincipal();
            String opcion = scanner.nextLine().trim();
            
            try {
                // 🔐 MENÚ DIFERENTE SEGÚN EL ROL
                if (SesionGlobal.get().esAdmin()) {
                    // ADMIN: puede ver Gestión de Lotes y Producción
                    switch (opcion) {
                        case "1":
                            menuGestionLotes();
                            break;
                        case "2":
                            menuProduccion();
                            break;
                        case "3":
                            salir = true;
                            cerrarSesion();
                            mostrarDespedida();
                            break;
                        default:
                            mostrarError("Opción no válida. Por favor, intenta de nuevo.");
                    }
                } else {
                    // OPERARIO: solo Registro de Producción
                    switch (opcion) {
                        case "1":
                            menuProduccion();
                            break;
                        case "2":
                            salir = true;
                            cerrarSesion();
                            mostrarDespedida();
                            break;
                        default:
                            mostrarError("Opción no válida. Por favor, intenta de nuevo.");
                    }
                }
            } catch (GranjaException e) {
                mostrarErrorGranja(e);
            } catch (Exception e) {
                mostrarErrorGeneral(e);
            }
        }
        
        scanner.close();
    }
    
    // ==================== MENÚ PRINCIPAL ====================
    
    /**
     * Muestra la pantalla de bienvenida.
     */
    private void mostrarBienvenida() {
        limpiarPantalla();
        System.out.println(ANSI_CYAN);
        System.out.println("════════════════════════════════════════════════════════════╗");
        System.out.println("                                                            ");
        System.out.println("               🐔 GRANJAPRO - GESTOR AVÍCOLA 🐔            ");
        System.out.println("                                                            ");
        System.out.println("          Versión 1.1 | Interfaz de Consola CLI            ");
      
        System.out.println(ANSI_RESET);
        System.out.println();
    }
    
    /**
     * Muestra el menú principal.
     */
    private void mostrarMenuPrincipal() {
        boolean esAdmin = SesionGlobal.get().esAdmin();
        String nombreUsuario = SesionGlobal.get().obtenerNombreUsuario();
        
        System.out.println(ANSI_BLUE + "┌─ MENÚ PRINCIPAL " + "─".repeat(40) + "┐" + ANSI_RESET);
        System.out.println("│ Usuario: " + nombreUsuario);
        System.out.println("│ Rol: " + (esAdmin ? "ADMIN" : "OPERARIO"));
        System.out.println("│");
        
        if (esAdmin) {
            System.out.println("│  1. Gestión de Lotes");
            System.out.println("│  2. Registro de Producción");
            System.out.println("│  3. Cerrar sesión y salir");
        } else {
            System.out.println("│  1. Registro de Producción");
            System.out.println("│  2. Cerrar sesión y salir");
        }
        
        System.out.println("│");
        System.out.println(ANSI_BLUE + "└" + "─".repeat(57) + "┘" + ANSI_RESET);
        System.out.print(ANSI_YELLOW + "Selecciona una opción: " + ANSI_RESET);
    }

    // 🔐 NUEVO: pantalla de login antes de entrar al menú
    private void mostrarLogin() {
        boolean logueado = false;

        while (!logueado) {
            limpiarPantalla();
            System.out.println(ANSI_CYAN + "╔════════════════════════════════════════════╗" + ANSI_RESET);
            System.out.println(ANSI_CYAN + "║          AUTENTICACIÓN GRANJAPRO          ║" + ANSI_RESET);
            System.out.println(ANSI_CYAN + "╚════════════════════════════════════════════╝" + ANSI_RESET);
            System.out.println();
            
            System.out.print("Usuario: ");
            String usuario = scanner.nextLine().trim();
            
            System.out.print("Contraseña: ");
            String password = scanner.nextLine().trim();
            
            try {
                Usuario usuarioLogueado = servicioSeguridad.login(usuario, password);
                System.out.println();
                mostrarExito("Bienvenido, " + usuarioLogueado.getNombre());
                logueado = true;
                
                System.out.println();
                System.out.print("Presiona ENTER para continuar...");
                scanner.nextLine();
            } catch (Exception e) {
                mostrarError(e.getMessage());
                System.out.println("Intenta de nuevo.");
                System.out.println();
                System.out.print("Presiona ENTER para continuar...");
                scanner.nextLine();
            }
        }
    }
    
    // ==================== MENÚ GESTIÓN LOTES ====================
    
    /**
     * Menú para gestión de lotes.
     */
    private void menuGestionLotes() {
        boolean volver = false;
        while (!volver) {
            limpiarPantalla();
            System.out.println(ANSI_BLUE + "┌─ GESTIÓN DE LOTES " + "─".repeat(38) + "┐" + ANSI_RESET);
            System.out.println("│");
            System.out.println("│  1. Crear nuevo lote");
            System.out.println("│  2. Registrar mortalidad");
            System.out.println("│  3. Ver todos los lotes");
            System.out.println("│  4. Ver detalles de un lote");
            System.out.println("│  5. Volver al menú principal");
            System.out.println("│");
            System.out.println(ANSI_BLUE + "└" + "─".repeat(57) + "┘" + ANSI_RESET);
            System.out.print(ANSI_YELLOW + "Selecciona una opción: " + ANSI_RESET);
            
            String opcion = scanner.nextLine().trim();
            
            try {
                switch (opcion) {
                    case "1":
                        crearLote();
                        break;
                    case "2":
                        registrarMortalidad();
                        break;
                    case "3":
                        verTodosLotes();
                        break;
                    case "4":
                        verDetallesLote();
                        break;
                    case "5":
                        volver = true;
                        break;
                    default:
                        mostrarError("Opción no válida.");
                }
            } catch (GranjaException e) {
                mostrarErrorGranja(e);
            } catch (Exception e) {
                mostrarErrorGeneral(e);
            }
        }
    }
    
    /**
     * Opción para crear un nuevo lote.
     */
    private void crearLote() {
        limpiarPantalla();
        System.out.println(ANSI_BLUE + "═══ CREAR NUEVO LOTE ═══" + ANSI_RESET);
        System.out.println();
        
        System.out.print("Código del lote: ");
        String codigo = scanner.nextLine().trim();
        
        System.out.print("Raza de gallinas: ");
        String raza = scanner.nextLine().trim();
        
        System.out.print("Cantidad inicial: ");
        Integer cantidad = leerInteger();
        
        System.out.print("ID del corral: ");
        String idCorral = scanner.nextLine().trim();
        
        Lote lote = servicioGestionLotes.crearLote(codigo, raza, cantidad, idCorral);
        
        System.out.println();
        mostrarExito("✅ Lote creado exitosamente");
        System.out.println(ANSI_GREEN + "  ID: " + lote.getId() + ANSI_RESET);
        System.out.println(ANSI_GREEN + "  Código: " + lote.getCodigo() + ANSI_RESET);
        System.out.println();
        System.out.print("Presiona ENTER para continuar...");
        scanner.nextLine();
    }
    
    /**
     * Opción para registrar mortalidad.
     */
    private void registrarMortalidad() {
        limpiarPantalla();
        System.out.println(ANSI_BLUE + "═══ REGISTRAR MORTALIDAD ═══" + ANSI_RESET);
        System.out.println();
        
        System.out.print("ID del lote: ");
        String idLote = scanner.nextLine().trim();
        
        System.out.print("Cantidad de muertes: ");
        Integer cantidad = leerInteger();
        
        servicioGestionLotes.registrarMortalidad(idLote, cantidad);
        
        System.out.println();
        mostrarExito("✅ Mortalidad registrada exitosamente");
        System.out.println();
        System.out.print("Presiona ENTER para continuar...");
        scanner.nextLine();
    }
    
    /**
     * Opción para ver todos los lotes.
     */
    private void verTodosLotes() {
        limpiarPantalla();
        System.out.println(ANSI_BLUE + "═══ LISTADO DE LOTES ═══" + ANSI_RESET);
        System.out.println();
        
        List<Lote> lotes = servicioGestionLotes.listarLotes();
        
        if (lotes.isEmpty()) {
            mostrarAdvertencia("No hay lotes registrados aún.");
        } else {
            System.out.println(ANSI_CYAN + String.format("%-40s %s", "Código", "Cantidad Actual") + ANSI_RESET);
            System.out.println("─".repeat(55));
            
            for (Lote lote : lotes) {
                System.out.println(String.format("%-40s %d", lote.getCodigo(), lote.getCantidadActual()));
            }
        }
        
        System.out.println();
        System.out.print("Presiona ENTER para continuar...");
        scanner.nextLine();
    }
    
    /**
     * Opción para ver detalles de un lote.
     */
    private void verDetallesLote() {
        limpiarPantalla();
        System.out.println(ANSI_BLUE + "═══ DETALLES DEL LOTE ═══" + ANSI_RESET);
        System.out.println();
        
        System.out.print("ID del lote: ");
        String idLote = scanner.nextLine().trim();
        
        Lote lote = servicioGestionLotes.obtenerLote(idLote);
        
        System.out.println();
        System.out.println(ANSI_GREEN + "┌─ INFORMACIÓN DEL LOTE " + "─".repeat(31) + "┐" + ANSI_RESET);
        System.out.println("│ ID: " + lote.getId());
        System.out.println("│ Código: " + lote.getCodigo());
        System.out.println("│ Raza: " + lote.getRaza());
        System.out.println("│ Cantidad Inicial: " + lote.getCantidadInicial());
        System.out.println("│ Cantidad Actual: " + lote.getCantidadActual());
        System.out.println("│ Fecha Ingreso: " + lote.getFechaIngreso());
        System.out.println("│ ID Corral: " + lote.getIdCorral());
        System.out.println(ANSI_GREEN + "└" + "─".repeat(55) + "┘" + ANSI_RESET);
        
        System.out.println();
        System.out.print("Presiona ENTER para continuar...");
        scanner.nextLine();
    }
    
    // ==================== MENÚ PRODUCCIÓN ====================
    
    /**
     * Menú para registro de producción.
     */
    private void menuProduccion() {
        boolean volver = false;
        while (!volver) {
            limpiarPantalla();
            System.out.println(ANSI_BLUE + "┌─ REGISTRO DE PRODUCCIÓN " + "─".repeat(32) + "┐" + ANSI_RESET);
            System.out.println("│");
            System.out.println("│  1. Registrar producción de huevos");
            System.out.println("│  2. Ver registros de un lote");
            System.out.println("│  3. Ver porcentaje de huevos rotos");
            System.out.println("│  4. Volver al menú principal");
            System.out.println("│");
            System.out.println(ANSI_BLUE + "└" + "─".repeat(57) + "┘" + ANSI_RESET);
            System.out.print(ANSI_YELLOW + "Selecciona una opción: " + ANSI_RESET);
            
            String opcion = scanner.nextLine().trim();
            
            try {
                switch (opcion) {
                    case "1":
                        registrarProduccion();
                        break;
                    case "2":
                        verRegistrosPorLote();
                        break;
                    case "3":
                        verPorcentajeRotos();
                        break;
                    case "4":
                        volver = true;
                        break;
                    default:
                        mostrarError("Opción no válida.");
                }
            } catch (GranjaException e) {
                mostrarErrorGranja(e);
            } catch (Exception e) {
                mostrarErrorGeneral(e);
            }
        }
    }
    
    /**
     * Opción para registrar producción de huevos.
     */
    private void registrarProduccion() {
        limpiarPantalla();
        System.out.println(ANSI_BLUE + "═══ REGISTRAR PRODUCCIÓN DE HUEVOS ═══" + ANSI_RESET);
        System.out.println();
        
        System.out.print("ID del lote: ");
        String idLote = scanner.nextLine().trim();
        
        System.out.print("Huevos totales: ");
        Integer huevosTotales = leerInteger();
        
        System.out.print("Huevos rotos: ");
        Integer huevosRotos = leerInteger();
        
        RegistroProduccion registro = servicioProduccion.registrarProduccion(
            idLote, huevosTotales, huevosRotos
        );
        
        System.out.println();
        mostrarExito("✅ Producción registrada exitosamente");
        System.out.println(ANSI_GREEN + "  Fecha: " + registro.getFecha() + ANSI_RESET);
        System.out.println(ANSI_GREEN + "  Huevos Totales: " + registro.getHuevosTotales() + ANSI_RESET);
        System.out.println(ANSI_GREEN + "  Huevos Rotos: " + registro.getHuevosRotos() + ANSI_RESET);
        System.out.println();
        System.out.print("Presiona ENTER para continuar...");
        scanner.nextLine();
    }
    
    /**
     * Opción para ver registros de producción de un lote.
     */
    private void verRegistrosPorLote() {
        limpiarPantalla();
        System.out.println(ANSI_BLUE + "═══ REGISTROS DE PRODUCCIÓN ═══" + ANSI_RESET);
        System.out.println();
        
        System.out.print("ID del lote: ");
        String idLote = scanner.nextLine().trim();
        
        List<RegistroProduccion> registros = servicioProduccion.obtenerRegistrosPorLote(idLote);
        
        System.out.println();
        if (registros.isEmpty()) {
            mostrarAdvertencia("No hay registros de producción para este lote.");
        } else {
            System.out.println(ANSI_CYAN + String.format("%-15s %s %s", "Fecha", "Totales", "Rotos") + ANSI_RESET);
            System.out.println("─".repeat(40));
            
            for (RegistroProduccion reg : registros) {
                System.out.println(String.format("%-15s %d %d",
                    reg.getFecha(), reg.getHuevosTotales(), reg.getHuevosRotos()));
            }
        }
        
        System.out.println();
        System.out.print("Presiona ENTER para continuar...");
        scanner.nextLine();
    }
    
    /**
     * Opción para ver porcentaje de huevos rotos.
     */
    private void verPorcentajeRotos() {
        limpiarPantalla();
        System.out.println(ANSI_BLUE + "═══ PORCENTAJE DE HUEVOS ROTOS ═══" + ANSI_RESET);
        System.out.println();
        
        System.out.print("ID del lote: ");
        String idLote = scanner.nextLine().trim();
        
        double porcentaje = servicioProduccion.calcularPorcentajeRotos(idLote);
        
        System.out.println();
        System.out.println(ANSI_GREEN + String.format("Porcentaje de huevos rotos: %.2f%%", porcentaje) + ANSI_RESET);
        System.out.println();
        System.out.print("Presiona ENTER para continuar...");
        scanner.nextLine();
    }
    
    // ==================== UTILIDADES ====================
    
    /**
     * Lee un número entero del scanner.
     */
    private Integer leerInteger() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            throw new GranjaException("Debes ingresar un número válido") {};
        }
    }
    
    /**
     * Muestra un mensaje de éxito.
     */
    private void mostrarExito(String mensaje) {
        System.out.println(ANSI_GREEN + mensaje + ANSI_RESET);
    }
    
    /**
     * Muestra un mensaje de error.
     */
    private void mostrarError(String mensaje) {
        System.out.println(ANSI_RED + "❌ " + mensaje + ANSI_RESET);
    }
    
    /**
     * Muestra un error de Granja (excepción personalizada).
     */
    private void mostrarErrorGranja(GranjaException e) {
        System.out.println();
        System.out.println(ANSI_RED + "╔════════════════════════════════════════════╗" + ANSI_RESET);
        System.out.println(ANSI_RED + "║ ❌ ERROR VALIDACIÓN" + ANSI_RESET);
        System.out.println(ANSI_RED + "╠════════════════════════════════════════════╣" + ANSI_RESET);
        System.out.println(ANSI_RED + "║ " + e.getMessage() + ANSI_RESET);
        System.out.println(ANSI_RED + "╚════════════════════════════════════════════╝" + ANSI_RESET);
        System.out.println();
        System.out.print("Presiona ENTER para continuar...");
        scanner.nextLine();
    }
    
    /**
     * Muestra un error genérico.
     */
    private void mostrarErrorGeneral(Exception e) {
        System.out.println();
        System.out.println(ANSI_RED + "╔════════════════════════════════════════════╗" + ANSI_RESET);
        System.out.println(ANSI_RED + "║ ❌ ERROR DEL SISTEMA" + ANSI_RESET);
        System.out.println(ANSI_RED + "╠════════════════════════════════════════════╣" + ANSI_RESET);
        System.out.println(ANSI_RED + "║ " + e.getMessage() + ANSI_RESET);
        System.out.println(ANSI_RED + "╚════════════════════════════════════════════╝" + ANSI_RESET);
        System.out.println();
        System.out.print("Presiona ENTER para continuar...");
        scanner.nextLine();
    }
    
    /**
     * Muestra un mensaje de advertencia.
     */
    private void mostrarAdvertencia(String mensaje) {
        System.out.println(ANSI_YELLOW + "⚠️  " + mensaje + ANSI_RESET);
    }
    
    /**
     * Muestra la pantalla de despedida.
     */
    private void mostrarDespedida() {
        limpiarPantalla();
        System.out.println(ANSI_CYAN);
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                            ║");
        System.out.println("║         ¡Gracias por usar GranjaPro! ¡Hasta luego!        ║");
        System.out.println("║                                                            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
        System.out.println(ANSI_RESET);
    }
    
    /**
     * Limpia la pantalla (funciona en Windows, Linux, macOS).
     */
    private void limpiarPantalla() {
        for (int i = 0; i < 50; i++) {
            System.out.println();
        }
    }

    // 🔐 NUEVO: cerrar sesión limpiando SesionGlobal
    private void cerrarSesion() {
        String nombre = SesionGlobal.get().obtenerNombreUsuario();
        System.out.println();
        System.out.println("Cerrando sesión de: " + nombre);
        servicioSeguridad.logout();
        System.out.println("Sesión cerrada.");
    }
    
    /**
     * Método principal para ejecutar la aplicación.
     */
    public static void main(String[] args) {
        ConsolaUi ui = new ConsolaUi();

        // 🔐 Primero pedimos login
        ui.mostrarLogin();

        // Solo iniciamos el menú si hay alguien logueado
        if (SesionGlobal.get().estaLogueado()) {
            ui.iniciar();
        }
    }
}
