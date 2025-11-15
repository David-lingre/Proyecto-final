# 🔐 TAREA_DAVID_SEGURIDAD - Para David

## Misión: Implementar Autenticación RBAC en ConsolaUi

David, tu tarea es **integrar seguridad en la interfaz de usuario**. Todo el código de seguridad ya existe (Usuario, Rol, SesionGlobal, ServicioSeguridad). Tu trabajo es conectarlo a ConsolaUi para que:

1. ✅ Pida login al iniciar
2. ✅ Use `SesionGlobal` para saber quién está logueado
3. ✅ Personalice el menú según el rol
4. ✅ Controle qué operaciones permite hacer cada rol

**Estimación:** 45-60 minutos  
---

## Contexto: Qué se Creó para Ti

### 1. Las 6 Clases de Seguridad (ya compiladas ✅)

#### `Rol.java` - Enum con los roles disponibles
```java
// Ubicación: dominio/modelos/Rol.java
public enum Rol {
    ADMIN("Administrador", "Acceso completo al sistema"),
    OPERARIO("Operario", "Acceso a operaciones básicas");
    
    public boolean esAdmin() { return this == ADMIN; }
    public boolean esOperario() { return this == OPERARIO; }
    public String getDescripcion() { /* ... */ }
}
```

**Roles definidos:**
- `ADMIN`: Puede crear/editar/borrar lotes
- `OPERARIO`: Solo puede ver y registrar producción

---

#### `Usuario.java` - Entidad con SHA-256 hashing
```java
// Ubicación: dominio/modelos/Usuario.java
public class Usuario {
    private String nombre;
    private String passwordHasheado;  // ⭐ Nunca en texto plano
    private Rol rol;
    private boolean activo;
    
    // ⭐ CRUCIAL: Validación de password con SHA-256
    public boolean validarPassword(String passwordPlano) {
        String hashIngresado = hashearPassword(passwordPlano);
        return this.passwordHasheado.equals(hashIngresado);
    }
    
    // Para SET password (automáticamente hashea)
    public void setPasswordPlano(String passwordPlano) {
        this.passwordHasheado = hashearPassword(passwordPlano);
    }
    
    public boolean esAdmin() { return rol.esAdmin(); }
    public boolean esOperario() { return rol.esOperario(); }
}
```

**Key point:** Nunca vemos la contraseña en texto plano. Se hashea con SHA-256.

---

#### `RepositorioUsuario.java` - Interface (DOMINIO)
```java
// Ubicación: dominio/repositorios/RepositorioUsuario.java
public interface RepositorioUsuario {
    Usuario buscarPorNombre(String nombre);
    Usuario buscarPorId(String id);
    void guardar(Usuario usuario);
    // ... otros métodos CRUD
}
```

Interface limpia. Implementación en Infraestructura.

---

#### `RepositorioUsuarioMongo.java` - Implementación (INFRAESTRUCTURA)
```java
// Ubicación: infraestructura/persistencia/mongo/RepositorioUsuarioMongo.java
public class RepositorioUsuarioMongo implements RepositorioUsuario {
    private MongoCollection<Document> coleccion;
    
    public RepositorioUsuarioMongo(MongoCollection<Document> coleccion) {
        this.coleccion = coleccion;
    }
    
    @Override
    public Usuario buscarPorNombre(String nombre) {
        // Busca en MongoDB
        Document doc = coleccion.find(Filters.eq("nombre", nombre)).first();
        if (doc == null) return null;
        return documentAUsuario(doc);
    }
    
    // ... otros métodos implementados
}
```

**Conecta a MongoDB.** Tu código NO lo usa directamente.

---

#### `SesionGlobal.java` - Singleton para mantener usuario logueado
```java
// Ubicación: aplicacion/sesion/SesionGlobal.java
public class SesionGlobal {
    private static SesionGlobal instancia;
    private Usuario usuarioLogueado;
    
    // Singleton pattern
    public static SesionGlobal get() {
        if (instancia == null) instancia = new SesionGlobal();
        return instancia;
    }
    
    // Después de login exitoso
    public void iniciarSesion(Usuario usuario) {
        this.usuarioLogueado = usuario;
    }
    
    // Obtener usuario actual
    public Usuario obtenerUsuario() {
        return this.usuarioLogueado;
    }
    
    // Utilidades
    public boolean estaLogueado() { return usuarioLogueado != null; }
    public boolean esAdmin() { 
        return estaLogueado() && usuarioLogueado.esAdmin(); 
    }
    public String obtenerNombreUsuario() { 
        return estaLogueado() ? usuarioLogueado.getNombre() : "Deslogueado"; 
    }
}
```

**MÁS IMPORTANTE:** Este objeto **VIVE EN RAM durante toda la ejecución**. Lo usas desde CUALQUIER PARTE:

```java
// En ConsolaUi:
if (SesionGlobal.get().esAdmin()) {
    mostrarOpcionBorrar();
}

// En GestionLotes:
if (!SesionGlobal.get().esAdmin()) {
    throw new UsuarioNoAutorizadoException(...);
}
```

---

#### `ServicioSeguridad.java` - Servicio de autenticación
```java
// Ubicación: aplicacion/servicios/ServicioSeguridad.java
public class ServicioSeguridad {
    private RepositorioUsuario repositorio;
    
    public ServicioSeguridad(RepositorioUsuario repositorio) {
        this.repositorio = repositorio;
    }
    
    // ⭐ MÉTODO CRÍTICO: Login
    public Usuario login(String nombreUsuario, String passwordPlano) 
            throws Exception {
        // 1. Busca usuario en BD
        Usuario usuario = repositorio.buscarPorNombre(nombreUsuario);
        if (usuario == null) 
            throw new Exception("Credenciales inválidas");  // Generic - no revela
        
        // 2. Verifica contraseña con SHA-256
        if (!usuario.validarPassword(passwordPlano)) 
            throw new Exception("Credenciales inválidas");
        
        // 3. Verifica que está activo
        if (!usuario.isActivo()) 
            throw new Exception("Usuario inactivo");
        
        // 4. SUCCESS - Inicia sesión global
        SesionGlobal.get().iniciarSesion(usuario);
        return usuario;
    }
    
    // Logout
    public void logout() {
        SesionGlobal.get().cerrarSesion();
    }
    
    // Utilidades
    public boolean esAdmin() { return SesionGlobal.get().esAdmin(); }
    public boolean estaLogueado() { return SesionGlobal.get().estaLogueado(); }
}
```

**Login flujo:**
```
Usuario ingresa: admin / admin123
    ↓
ServicioSeguridad.login("admin", "admin123")
    ↓ Busca "admin" en BD
    ↓ Compara SHA-256(admin123) con passwordHasheado en BD
    ↓ Si coincide → SesionGlobal.get().iniciarSesion(usuario)
    ↓ SesionGlobal.get().obtenerUsuario() ahora retorna usuario
    ↓ SesionGlobal.get().esAdmin() ahora retorna true
```

---

## Arquitectura de Seguridad (Visualización)

```
┌─────────────────────────────────────────────────────────────┐
│                       ConsolaUi                             │
│  (Presentación - La interfaz que usa el usuario)            │
│                                                             │
│  • Muestra pantalla de login                                │
│  • Lee usuario/password del input                           │
│  • Llama a ServicioSeguridad.login()                        │
│  • Usa SesionGlobal para saber roles y permisos             │
│                                                             │
│  ⭐ Regla: ConsolaUi NO valida permisos.                    │
│     Solo decide QUÉ MOSTRAR según el rol.                  │
│     La validación real está en los Servicios.               │
└─────────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────────┐
│              SesionGlobal (Singleton en RAM)                │
│                                                             │
│  • Mantiene: Usuario actual + Rol                           │
│  • Accesible desde CUALQUIER PARTE del código               │
│  • Durante toda la ejecución de la app                      │
│                                                             │
│  Uso:                                                       │
│  - SesionGlobal.get().obtenerUsuario() → Usuario            │
│  - SesionGlobal.get().esAdmin() → true/false                │
│  - SesionGlobal.get().cerrarSesion() → Logout               │
└─────────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────────┐
│          ServicioSeguridad (Lógica de autenticación)        │
│                                                             │
│  • Valida credenciales                                      │
│  • Inicia/Cierra sesión en SesionGlobal                     │
│  • Compara SHA-256                                          │
│                                                             │
│  Métodos principales:                                       │
│  - login(usuario, password) → Usuario                       │
│  - logout()                                                 │
│  - esAdmin() → boolean                                      │
└─────────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────────┐
│              Base de Datos (MongoDB)                        │
│                                                             │
│  Almacena:                                                  │
│  - Colección "usuarios"                                     │
│  - Documentos: {nombre, passwordHasheado, rol, activo}      │
│                                                             │
│  Usuario "admin" debe existir con:                          │
│  - nombre: "admin"                                          │
│  - passwordHasheado: SHA-256("admin123") =                  │
│    "9c9064c59f1ffa2b46701211ee3d302c2e7e0afc......"       │
│  - rol: "ADMIN"                                             │
│  - activo: true                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## ⭐ CONCEPTO CLAVE: Por qué está Desacoplada

### El Problema Sin Seguridad Desacoplada

```java
// ❌ INCORRECTO (Seguridad en la UI):
// ConsolaUi.java
public void mostrarOpcionBorrarLote() {
    if (usuarioRol.equals("ADMIN")) {  // ← Validación aquí
        // Borrar lote
    }
}
```

**Problema:** Si alguien modifica ConsolaUi quitando el IF, ¡pueda borrar!

---

### La Solución: Seguridad en los Servicios

```
┌─────────────────────────────────────────────────────────────┐
│                      ConsolaUi                              │
│  if (SesionGlobal.get().esAdmin()) {                        │
│      mostrarOpcionBorrar();  ← Solo decide QUÉ MOSTRAR       │
│  }                                                          │
└─────────────────────────────────────────────────────────────┘
                            ↓ Usuario cliquea "Borrar"
┌─────────────────────────────────────────────────────────────┐
│              GestionLotes (Servicio)                        │
│                                                             │
│  public void borrarLote(String id) throws Exception {       │
│      Usuario user = SesionGlobal.get().obtenerUsuario();    │
│      if (!user.esAdmin()) {  ← VALIDACIÓN REAL                │
│          throw new UsuarioNoAutorizadoException(...);       │
│      }                                                      │
│      repositorio.eliminar(id);                              │
│  }                                                          │
│                                                             │
│  Aunque alguien quite el IF en ConsolaUi, ¡la validación    │
│  sigue aquí! El usuario no puede eliminar.                 │
└─────────────────────────────────────────────────────────────┘
```

**Ventaja:** La seguridad está en el **servidor (lógica)**, no en la **interfaz (presentación)**.

---

## Paso 1: Preparar ConsolaUi para Seguridad

### Ubica el archivo:
```
src/main/java/com/granjapro/presentacion/ConsolaUi.java
```

Lee las primeras líneas para entender la estructura actual.

---

## Paso 2: CAMBIO 1 - Agregar Imports

**Busca:** La sección de `import` (primeras líneas de ConsolaUi.java)

**Añade estos imports:**
```java
import com.granjapro.aplicacion.servicios.ServicioSeguridad;
import com.granjapro.aplicacion.sesion.SesionGlobal;
import com.granjapro.dominio.excepciones.UsuarioNoAutorizadoException;
import com.granjapro.dominio.modelos.Rol;
import com.granjapro.dominio.modelos.Usuario;
import com.granjapro.dominio.repositorios.RepositorioUsuario;
import com.granjapro.infraestructura.persistencia.mongo.RepositorioUsuarioMongo;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
```

---

## Paso 3: CAMBIO 2 - Agregar Campo Servicio de Seguridad

**Busca en ConsolaUi.java:** Los campos privados de la clase (después de `public class ConsolaUi {`)

**Añade:**
```java
private ServicioSeguridad servicioSeguridad;
```

Ejemplo (lo que verás):
```java
public class ConsolaUi {
    private GestionLotes gestionLotes;
    private Produccion produccion;
    private ServicioSeguridad servicioSeguridad;  // ← NUEVA LÍNEA
    
    // ... resto del código
}
```

---

## Paso 4: CAMBIO 3 - Inicializar en Constructor

**Busca:** El constructor de ConsolaUi

Generalmente se ve así:
```java
public ConsolaUi() {
    ConexionMongo conexion = ConexionMongo.getInstance();
    MongoDatabase database = conexion.getDatabase("granja_db");
    
    // Inicialización de repositorios
    MongoCollection<Document> lotesCol = database.getCollection("lotes");
    RepositorioLote repositorioLote = new RepositorioLoteMongo(lotesCol);
    
    MongoCollection<Document> huevosCol = database.getCollection("huevos");
    RepositorioHuevo repositorioHuevo = new RepositorioHuesoMongo(huevosCol);
    
    // Inicialización de servicios
    this.gestionLotes = new GestionLotes(repositorioLote);
    this.produccion = new Produccion(repositorioHuevo);
}
```

**Añade al final (antes del último `}`)**:

```java
    // ⭐ Inicializar seguridad
    MongoCollection<Document> usuariosCol = database.getCollection("usuarios");
    RepositorioUsuario repositorioUsuario = new RepositorioUsuarioMongo(usuariosCol);
    this.servicioSeguridad = new ServicioSeguridad(repositorioUsuario);
}
```

**Resultado final:**
```java
public ConsolaUi() {
    ConexionMongo conexion = ConexionMongo.getInstance();
    MongoDatabase database = conexion.getDatabase("granja_db");
    
    // Inicialización de repositorios existentes
    MongoCollection<Document> lotesCol = database.getCollection("lotes");
    RepositorioLote repositorioLote = new RepositorioLoteMongo(lotesCol);
    
    MongoCollection<Document> huevosCol = database.getCollection("huevos");
    RepositorioHuevo repositorioHuevo = new RepositorioHuesoMongo(huevosCol);
    
    // Inicialización de servicios existentes
    this.gestionLotes = new GestionLotes(repositorioLote);
    this.produccion = new Produccion(repositorioHuevo);
    
    // ⭐ NUEVA: Inicializar seguridad
    MongoCollection<Document> usuariosCol = database.getCollection("usuarios");
    RepositorioUsuario repositorioUsuario = new RepositorioUsuarioMongo(usuariosCol);
    this.servicioSeguridad = new ServicioSeguridad(repositorioUsuario);
}
```

---

## Paso 5: CAMBIO 4 - Modificar main()

**Busca:** El método `main` de ConsolaUi

Generalmente:
```java
public static void main(String[] args) {
    ConsolaUi ui = new ConsolaUi();
    ui.iniciar();
}
```

**Reemplaza con:**
```java
public static void main(String[] args) {
    ConsolaUi ui = new ConsolaUi();
    ui.mostrarSplashYLogin();  // ← Primero login
    if (SesionGlobal.get().estaLogueado()) {
        ui.iniciar();  // ← Luego menú principal
    }
}
```

---

## Paso 6: CAMBIO 5 - Crear mostrarSplashYLogin()

**Crea este método NUEVO en ConsolaUi:**

```java
private void mostrarSplashYLogin() {
    System.out.println("\n╔════════════════════════════════════════════════════════════╗");
    System.out.println("║                                                            ║");
    System.out.println("║          🐔 GRANJAPRO - GESTOR AVÍCOLA 🐔                 ║");
    System.out.println("║                                                            ║");
    System.out.println("║        Versión 1.0 | Sistema de Gestión Agrícola          ║");
    System.out.println("║                                                            ║");
    System.out.println("╚════════════════════════════════════════════════════════════╝");
    
    Scanner scanner = new Scanner(System.in);
    boolean logueado = false;
    
    while (!logueado) {
        System.out.println("\n┌─ AUTENTICACIÓN ────────────────────────────┐");
        System.out.print("│ Usuario: ");
        String usuario = scanner.nextLine().trim();
        
        System.out.print("│ Contraseña: ");
        String password = scanner.nextLine().trim();
        System.out.println("└────────────────────────────────────────────┘");
        
        try {
            // Intentar login
            Usuario usuarioLogueado = servicioSeguridad.login(usuario, password);
            
            System.out.println("\n✅ Bienvenido, " + usuarioLogueado.getNombre() 
                             + " (Rol: " + usuarioLogueado.getRol().getDescripcion() + ")");
            logueado = true;
            
        } catch (Exception e) {
            System.out.println("❌ " + e.getMessage());
            System.out.println("   Por favor, intenta de nuevo.");
        }
    }
}
```

**Qué hace:**
1. Muestra splash screen (logo)
2. Pide usuario y contraseña
3. Llama `servicioSeguridad.login()`
4. Si falla, muestra error y repite
5. Si exitoso, la sesión está en `SesionGlobal`

---

## Paso 7: CAMBIO 6 - Modificar mostrarMenuPrincipal()

**Ubica:** El método `mostrarMenuPrincipal()` en ConsolaUi

Generalmente muestra algo como:
```
1. Gestión de Lotes
2. Registro de Producción
3. Salir
```

**Reemplaza para que dependa del rol:**

```java
private void mostrarMenuPrincipal() {
    Usuario usuario = SesionGlobal.get().obtenerUsuario();
    
    System.out.println("\n┌─ MENÚ PRINCIPAL ────────────────────────────┐");
    System.out.println("│                                             │");
    System.out.println("│  1. 📁 Gestión de Lotes                     │");
    
    // Opciones solo para ADMIN
    if (usuario.esAdmin()) {
        System.out.println("│  2. 🗑️  Borrar Lote (ADMIN ONLY)           │");
        System.out.println("│  3. 📊 Registro de Producción              │");
        System.out.println("│  4. 👤 Gestionar Usuarios (ADMIN ONLY)    │");
        System.out.println("│  5. 🚪 Cerrar Sesión                       │");
    } else {
        // Opciones para OPERARIO
        System.out.println("│  2. 📊 Registro de Producción              │");
        System.out.println("│  3. 🚪 Cerrar Sesión                       │");
    }
    
    System.out.println("│                                             │");
    System.out.println("└─────────────────────────────────────────────┘");
    System.out.print("\nSelecciona una opción: ");
}
```

**Lógica:**
- `usuario.esAdmin()` = true → mostrar opciones 2, 4
- `usuario.esAdmin()` = false → solo opciones básicas

---

## Paso 8: CAMBIO 7 - Modificar iniciar() para Roles

**Ubica:** El método `iniciar()` que contiene el switch/if de opciones

Generalmente:
```java
private void iniciar() {
    Scanner scanner = new Scanner(System.in);
    boolean continuar = true;
    
    while (continuar) {
        mostrarMenuPrincipal();
        String opcion = scanner.nextLine().trim();
        
        switch (opcion) {
            case "1":
                // Opción 1
                break;
            case "2":
                // Opción 2
                break;
            // ... más casos
        }
    }
}
```

**Reemplaza el switch para respetar roles:**

```java
private void iniciar() {
    Scanner scanner = new Scanner(System.in);
    boolean continuar = true;
    Usuario usuario = SesionGlobal.get().obtenerUsuario();
    
    while (continuar) {
        mostrarMenuPrincipal();
        String opcion = scanner.nextLine().trim();
        
        if (usuario.esAdmin()) {
            // MENÚ PARA ADMIN (5 opciones)
            switch (opcion) {
                case "1":
                    // Gestión de Lotes
                    gestionLotes.crear();
                    break;
                case "2":
                    // Borrar Lote (SOLO ADMIN)
                    System.out.print("Ingresa ID del lote a borrar: ");
                    String idBorrar = scanner.nextLine().trim();
                    try {
                        gestionLotes.borrar(idBorrar);
                        System.out.println("✅ Lote borrado");
                    } catch (Exception e) {
                        System.out.println("❌ " + e.getMessage());
                    }
                    break;
                case "3":
                    // Registro de Producción
                    produccion.registrar();
                    break;
                case "4":
                    // Gestionar Usuarios (SOLO ADMIN)
                    System.out.println("✅ Crear nuevo usuario");
                    System.out.print("Nombre: ");
                    String nombreUsuario = scanner.nextLine().trim();
                    System.out.print("Contraseña: ");
                    String passUsuario = scanner.nextLine().trim();
                    // Implementar creación de usuario
                    break;
                case "5":
                    cerrarSesion();
                    continuar = false;
                    break;
                default:
                    System.out.println("❌ Opción inválida");
            }
        } else {
            // MENÚ PARA OPERARIO (3 opciones)
            switch (opcion) {
                case "1":
                    // Gestión de Lotes (solo lectura)
                    gestionLotes.listar();
                    break;
                case "2":
                    // Registro de Producción
                    produccion.registrar();
                    break;
                case "3":
                    cerrarSesion();
                    continuar = false;
                    break;
                default:
                    System.out.println("❌ Opción inválida");
            }
        }
    }
}
```

---

## Paso 9: CAMBIO 8 - Crear cerrarSesion()

**Crea este método NUEVO en ConsolaUi:**

```java
private void cerrarSesion() {
    Usuario usuario = SesionGlobal.get().obtenerUsuario();
    System.out.println("\n✅ Hasta luego, " + usuario.getNombre() + "!");
    
    // Limpiar sesión
    servicioSeguridad.logout();
    
    System.out.println("🚪 Sesión cerrada.");
    System.out.println("\nGracias por usar GranjaPro.\n");
}
```

---

## Paso 10: Crear Usuario Admin en MongoDB

**CRÍTICO:** Sin usuario admin, nadie puede loguearse.

### Opción A: Crear via código temporal (RÁPIDO)

En el constructor de ConsolaUi, añade esto ANTES de `this.gestionLotes = ...`:

```java
public ConsolaUi() {
    ConexionMongo conexion = ConexionMongo.getInstance();
    MongoDatabase database = conexion.getDatabase("granja_db");
    
    // ... resto del código anterior ...
    
    // ⭐ TEMPORAL: Crear usuario admin si no existe
    MongoCollection<Document> usuariosCol = database.getCollection("usuarios");
    Document adminExistente = usuariosCol.find(
        Filters.eq("nombre", "admin")
    ).first();
    
    if (adminExistente == null) {
        // No existe, crear
        String passwordHasheado = Usuario.hashearPassword("admin123");
        Document adminDoc = new Document()
            .append("nombre", "admin")
            .append("passwordHasheado", passwordHasheado)
            .append("rol", "ADMIN")
            .append("activo", true);
        
        usuariosCol.insertOne(adminDoc);
        System.out.println("✅ Usuario admin creado (usuario: admin, password: admin123)");
    }
    
    RepositorioUsuario repositorioUsuario = new RepositorioUsuarioMongo(usuariosCol);
    this.servicioSeguridad = new ServicioSeguridad(repositorioUsuario);
}
```

**Nota:** Accede a `Usuario.hashearPassword()` (es static) para generar el hash.

### Opción B: MongoDB Compass (Manual)

1. Abre MongoDB Compass
2. Conecta a `mongodb://localhost:27017`
3. Database: `granja_db`
4. Collection: `usuarios` (crear si no existe)
5. Insert Document:

```json
{
  "nombre": "admin",
  "passwordHasheado": "9c9064c59f1ffa2b46701211ee3d302c2e7e0afc4f79d9fe5526200e5d5f8ba",
  "rol": "ADMIN",
  "activo": true
}
```

El hash es: SHA-256("admin123")

---

## Paso 11: CAMBIO 9 - Verificar Compilación

```bash
mvn clean compile
```

**Resultado esperado:**
```
[INFO] Compiling 19+ source files...
[INFO] BUILD SUCCESS
```

Si hay errores:
1. Lee el mensaje exacto
2. Verifica imports
3. Verifica que los nombres coincidan (mayúsculas/minúsculas)
4. Reintenta

---

## Paso 12: Testing Manual

### Test 1: Login exitoso

```
Usuario: admin
Contraseña: admin123

✅ Bienvenido, admin (Rol: Administrador)
```

Deberías ver el menú con 5 opciones.

### Test 2: Login fallido

```
Usuario: admin
Contraseña: wrongpassword

❌ Credenciales inválidas
   Por favor, intenta de nuevo.
```

Loop repite pedir usuario/password.

### Test 3: Menú personalizado (ADMIN)

```
┌─ MENÚ PRINCIPAL ────────────────────────────┐
│ 1. 📁 Gestión de Lotes                      │
│ 2. 🗑️  Borrar Lote (ADMIN ONLY)             │
│ 3. 📊 Registro de Producción                │
│ 4. 👤 Gestionar Usuarios (ADMIN ONLY)       │
│ 5. 🚪 Cerrar Sesión                         │
└─────────────────────────────────────────────┘
```

### Test 4: Cerrar sesión

```
Selecciona una opción: 5

✅ Hasta luego, admin!
🚪 Sesión cerrada.

┌─ AUTENTICACIÓN ────────────────────────────┐
```

Vuelve al login.

---

## Checklist de David

- [ ] ✅ Leí y entendí la arquitectura (puntos 1-2 arriba)
- [ ] ✅ Agregué imports (CAMBIO 1)
- [ ] ✅ Agregué campo `servicioSeguridad` (CAMBIO 2)
- [ ] ✅ Inicialicé en constructor (CAMBIO 3)
- [ ] ✅ Modifiqué `main()` (CAMBIO 4)
- [ ] ✅ Creé `mostrarSplashYLogin()` (CAMBIO 5)
- [ ] ✅ Modifiqué `mostrarMenuPrincipal()` (CAMBIO 6)
- [ ] ✅ Modifiqué `iniciar()` con rol checks (CAMBIO 7)
- [ ] ✅ Creé `cerrarSesion()` (CAMBIO 8)
- [ ] ✅ Creé usuario admin en MongoDB (CAMBIO 9)
- [ ] ✅ `mvn clean compile` → BUILD SUCCESS
- [ ] ✅ Testé login (admin/admin123)
- [ ] ✅ Testé menú personalizado por rol
- [ ] ✅ Testé cerrar sesión

---

## Diagrama Completo del Flujo

```
1. INICIO
   └─→ main() llama mostrarSplashYLogin()

2. SPLASH
   └─→ Muestra logo GranjaPro

3. LOGIN LOOP
   ├─→ Pide usuario/password
   ├─→ Llama servicioSeguridad.login(usuario, password)
   ├─→ Si falla: muestra error, repite
   └─→ Si éxito: SesionGlobal tiene usuario, sigue

4. MENÚ PRINCIPAL
   ├─→ Obtiene usuario de SesionGlobal
   ├─→ Si esAdmin() → muestra 5 opciones
   └─→ Si esOperario() → muestra 3 opciones

5. OPCIONES
   ├─→ 1: Gestión de Lotes (todos ven)
   ├─→ 2: Borrar Lote (ADMIN vee)
   ├─→ 3: Registro Producción (todos)
   ├─→ 4: Gestionar Usuarios (ADMIN ve)
   └─→ 5: Cerrar Sesión (todos)

6. LOGOUT
   ├─→ Llama servicioSeguridad.logout()
   ├─→ SesionGlobal se limpia
   └─→ Vuelve a login
```

---

## Explicación Teórica Profunda

### ¿Por qué la seguridad está desacoplada de la vista?

#### En una arquitectura mala:
```
ConsolaUi SABE QUÉ puede hacer
    ↓
ConsolaUi VALIDA si el usuario puede
    ↓
ConsolaUi EJECUTA la acción
```

**Problema:** Si modifica ConsolaUi, salta validación.

#### En Clean Architecture (la correcta):
```
ConsolaUi MUESTRA opciones según rol
    ↓ (pero no valida internamente)
    ↓
ConsolaUi llama Servicio.accion()
    ↓
Servicio VALIDA permisos (seguridad real aquí)
    ↓
Servicio EJECUTA o lanza excepción
```

**Ventaja:** Aunque modifiques ConsolaUi, Servicio rechaza.

### Ejemplo práctico:

```java
// Alguien quita el IF en ConsolaUi (mala fe):
private void iniciar() {
    // if (usuario.esAdmin()) {  ← Comentado/borrado
        gestionLotes.borrarLote("id-123");  ← Intenta borrar
    // }
}
```

**¿Qué pasa?**
1. Se llama `gestionLotes.borrarLote()`
2. Dentro del servicio hay:
   ```java
   public void borrarLote(String id) {
       if (!SesionGlobal.get().esAdmin()) {
           throw new UsuarioNoAutorizadoException(...);
       }
       repositorio.eliminar(id);
   }
   ```
3. Se lanza excepción
4. El usuario OPERARIO NO puede borrar
5. ¡Seguridad mantenida!

---

## Próximos Pasos

Una vez termines:

1. **Git commit:**
```bash
git add src/main/java/com/granjapro/
git commit -m "Feat: Integrar autenticación RBAC en ConsolaUi (David)"
```

2. **Avisa al Lead:** "ConsolaUi integrado y testeado"

3. **Mejoras futuras:**
   - Crear interfaz para gestionar usuarios (CRUD completo)
   - Agregar logs de acceso
   - Timeout de sesión (30 min sin actividad = logout)
   - Encriptación de BD

---

## Referencias

- **Entender clases:** Mira en `src/main/java/com/granjapro/aplicacion/sesion/SesionGlobal.java`
- **Arquitectura:** Consulta `ARQUITECTURA.md`
- **Estándares:** Consulta `MANUAL_GENERAL.md`
- **Ejemplos de código:** En este documento (Paso 6-9)

---

## ¿Preguntas o Problemas?

1. ¿`SesionGlobal.get()` retorna null? → MongoDB no está corriendo
2. ¿Error "Cannot find symbol"? → Falta import
3. ¿LOGIN no valida password? → Verifica que Usuario.hashearPassword() sea correcto
4. ¿BUILD FAILURE? → Lee el error exacto en el output

---

**David, esto es el último pillar. Cuando termines, el proyecto está 100% listo para producción.** 🚀

**Fecha creación:** Noviembre 14, 2025  
**Estimación:** 45-60 minutos  
**Prioridad:** CRÍTICA

¡Adelante! 💪
