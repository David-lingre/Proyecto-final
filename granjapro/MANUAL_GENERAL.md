# 📖 MANUAL GENERAL - Estándares de GranjaPro

## Regla #1 GIGANTE: ❌ NO USAR LOMBOK

**NUNCA importes Lombok en este proyecto.** Este es un curso de **POO Puro**, no de conveniencias.

### ❌ INCORRECTO (Lombok)

```java
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Lote {
    private String codigo;
    private Integer cantidadInicial;
    private String raza;
}
```

**Problemas:**
- Oculta la lógica de getters/setters
- Es "magia" - no ves qué genera
- En examen no puedes usarlo
- No aprendes POO de verdad

### ✅ CORRECTO (Java Beans)

```java
public class Lote {
    private String codigo;
    private Integer cantidadInicial;
    private String raza;
    
    // Constructor sin argumentos
    public Lote() {
        this.codigo = "";
        this.cantidadInicial = 0;
        this.raza = "";
    }
    
    // Constructor con argumentos
    public Lote(String codigo, Integer cantidadInicial, String raza) {
        this.codigo = codigo;
        this.cantidadInicial = cantidadInicial;
        this.raza = raza;
    }
    
    // Getters
    public String getCodigo() {
        return this.codigo;
    }
    
    public Integer getCantidadInicial() {
        return this.cantidadInicial;
    }
    
    public String getRaza() {
        return this.raza;
    }
    
    // Setters CON VALIDACIÓN
    public void setCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Código no puede estar vacío");
        }
        this.codigo = codigo;
    }
    
    public void setCantidadInicial(Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("Cantidad debe ser > 0");
        }
        this.cantidadInicial = cantidad;
    }
    
    public void setRaza(String raza) {
        if (raza == null || raza.isBlank()) {
            throw new IllegalArgumentException("Raza no puede estar vacía");
        }
        this.raza = raza;
    }
}
```

---

## Estándar de Java Beans

**Java Beans = Convención de naming para getters/setters**

### Reglas

| Tipo | Getter | Setter | Ejemplo |
|---|---|---|---|
| Atributo `String` | `get` + Nombre | `set` + Nombre | `getCodigo()`, `setCodigo()` |
| Atributo `boolean` | `is` + Nombre | `set` + Nombre | `isActivo()`, `setActivo()` |
| Atributo `Integer` | `get` + Nombre | `set` + Nombre | `getCantidad()`, `setCantidad()` |

### Ejemplo Completo

```java
public class Usuario {
    private String nombre;
    private boolean activo;
    private Integer edad;
    private String passwordHasheado;
    private Rol rol;
    
    // Getters
    public String getNombre() { return this.nombre; }
    public boolean isActivo() { return this.activo; }  // ⭐ Nota: is, no get
    public Integer getEdad() { return this.edad; }
    public String getPasswordHasheado() { return this.passwordHasheado; }
    public Rol getRol() { return this.rol; }
    
    // Setters
    public void setNombre(String nombre) {
        if (nombre == null || nombre.length() < 3) {
            throw new IllegalArgumentException("Nombre debe tener >= 3 caracteres");
        }
        this.nombre = nombre;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    public void setEdad(Integer edad) {
        if (edad == null || edad < 18 || edad > 120) {
            throw new IllegalArgumentException("Edad inválida");
        }
        this.edad = edad;
    }
    
    public void setPasswordHasheado(String password) {
        if (password == null || password.length() < 60) {
            throw new IllegalArgumentException("Password hash inválido");
        }
        this.passwordHasheado = password;
    }
    
    public void setRol(Rol rol) {
        if (rol == null) {
            throw new IllegalArgumentException("Rol no puede ser null");
        }
        this.rol = rol;
    }
}
```

---

## Validación en Setters

**REGLA:** Todo dato entra por un setter. El setter valida. No hay excepciones.

### Validaciones Típicas

```java
// 1. No null
if (valor == null) throw new IllegalArgumentException("...");

// 2. No blanco/vacío (para String)
if (valor.isBlank()) throw new IllegalArgumentException("...");

// 3. Rango numérico
if (valor < 0 || valor > 1000) throw new IllegalArgumentException("...");

// 4. Patrón (regex)
if (!valor.matches("^[A-Z]-\\d{3}$")) 
    throw new IllegalArgumentException("Código debe ser como A-001");

// 5. Unicidad (consulta BD)
if (repositorio.yaExiste(valor)) 
    throw new IllegalArgumentException("Valor duplicado");
```

### Ejemplo: Validar cantidad de gallinas

```java
public class Lote {
    private Integer cantidadInicial;
    
    public void setCantidadInicial(Integer cantidad) {
        // Validación 1: No null
        if (cantidad == null) {
            throw new IllegalArgumentException(
                "Cantidad no puede ser null"
            );
        }
        
        // Validación 2: > 0
        if (cantidad <= 0) {
            throw new IllegalArgumentException(
                "Cantidad debe ser mayor a 0. Recibiste: " + cantidad
            );
        }
        
        // Validación 3: Máximo realista (granjas no tienen millones)
        if (cantidad > 10000) {
            throw new IllegalArgumentException(
                "Cantidad muy alta (máximo 10.000). Recibiste: " + cantidad
            );
        }
        
        this.cantidadInicial = cantidad;
    }
}
```

---

## Estructura de Excepciones

### Jerarquía Recomendada

```
Exception
  └─ GranjaException (✅ personalizada del dominio)
       ├─ LoteNoEncontradoException
       ├─ CantidadInvalidaException
       ├─ DatoInvalidoException
       ├─ ProduccionInvalidaException
       └─ UsuarioNoAutorizadoException
```

### Código Base

```java
// GranjaException.java (clase base)
public class GranjaException extends Exception {
    public GranjaException(String mensaje) {
        super(mensaje);
    }
    
    public GranjaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}

// LoteNoEncontradoException.java
public class LoteNoEncontradoException extends GranjaException {
    public LoteNoEncontradoException(String id) {
        super("Lote con ID " + id + " no encontrado");
    }
}

// CantidadInvalidaException.java
public class CantidadInvalidaException extends GranjaException {
    public CantidadInvalidaException(String mensaje) {
        super(mensaje);
    }
}
```

### Usar en el Código

```java
try {
    Lote lote = new Lote();
    lote.setCantidadInicial(-50);  // ❌ Lanza excepción
} catch (IllegalArgumentException e) {
    System.out.println("❌ Error: " + e.getMessage());
}

// O en un repositorio:
Lote lote = repositorio.buscarPorId("xyz");
if (lote == null) {
    throw new LoteNoEncontradoException("xyz");
}
```

---

## Patrones de Codificación

### 1. Constructores

```java
public class Lote {
    private String codigo;
    private Integer cantidad;
    private String raza;
    
    // Constructor sin argumentos (always include)
    public Lote() {
        this.codigo = "";
        this.cantidad = 0;
        this.raza = "";
    }
    
    // Constructor con argumentos (parametrizado)
    public Lote(String codigo, Integer cantidad, String raza) {
        setCodigo(codigo);           // ⭐ Usa setters (ejecuta validación)
        setCantidad(cantidad);
        setRaza(raza);
    }
}
```

### 2. Métodos Útiles

```java
// toString() - siempre incluye
@Override
public String toString() {
    return "Lote{" +
        "codigo='" + codigo + '\'' +
        ", cantidad=" + cantidad +
        ", raza='" + raza + '\'' +
        '}';
}

// equals() - para comparar objetos
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Lote lote = (Lote) o;
    return Objects.equals(codigo, lote.codigo);
}

// hashCode() - para usar en HashMap/HashSet
@Override
public int hashCode() {
    return Objects.hash(codigo);
}
```

### 3. Inyección de Dependencias

```java
// ✅ CORRECTO:
public class GestionLotes {
    private RepositorioLote repositorio;
    
    // Constructor recibe dependencia
    public GestionLotes(RepositorioLote repositorio) {
        if (repositorio == null) {
            throw new IllegalArgumentException("Repositorio no puede ser null");
        }
        this.repositorio = repositorio;
    }
}

// ❌ INCORRECTO:
public class GestionLotes {
    private RepositorioLote repositorio = new RepositorioLoteMongo();  // ❌ Acoplada
}
```

### 4. Métodos Query vs Command

```java
// Query (retorna datos, no cambia estado)
public Lote buscarPorId(String id) { 
    return repositorio.buscarPorId(id); 
}

// Command (cambia estado, puede retornar o no)
public void crearLote(String codigo, Integer cantidad) {
    Lote lote = new Lote(codigo, cantidad, "RIR");
    repositorio.guardar(lote);
}

// Mix (crea y retorna)
public Lote crearYGuardar(String codigo, Integer cantidad) {
    Lote lote = new Lote(codigo, cantidad, "RIR");
    repositorio.guardar(lote);
    return lote;
}
```

---

## Naming Conventions

### Variables y Métodos (camelCase)

```java
int cantidadGallinas;          // ✅ Variable
int cantidad_gallinas;         // ❌ Snake case (Python style)
int CANTIDAD_GALLINAS;         // ❌ SCREAMING_SNAKE (constantes)

void crearLote() { }           // ✅ Método
void crear_lote() { }          // ❌ Snake case
void CreateLote() { }          // ❌ PascalCase (reservado para clases)
```

### Clases (PascalCase)

```java
public class Lote { }          // ✅
public class lote { }          // ❌ Minúscula
public class LOTE { }          // ❌ MAYÚSCULA
```

### Constantes (SCREAMING_SNAKE_CASE)

```java
public static final int CANTIDAD_MAXIMA = 10000;      // ✅
public static final int cantidadMaxima = 10000;       // ❌
public static final String BD_URL = "mongodb://...";  // ✅
```

### Booleanos (is/has/can prefix)

```java
boolean isActivo;              // ✅ "es activo"
boolean hasPermiso;            // ✅ "tiene permiso"
boolean canDelete;             // ✅ "puede borrar"
boolean activo;                // ⚠️ Ambiguo
boolean estado;                // ❌ Pésimo
```

---

## Cómo Correr los Tests

### Verificar que existen tests

```bash
# En Windows PowerShell
ls src/test/java -Recurse -Filter "*Test.java"
```

### Ejecutar todos los tests

```bash
mvn test
```

**Resultado esperado:**
```
[INFO] Running com.granjapro.AppTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running com.granjapro.service.OperacionesServiceTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Ejecutar un test específico

```bash
mvn test -Dtest=AppTest
mvn test -Dtest=OperacionesServiceTest
mvn test -Dtest=AppTest#testCrearLote    # Un método específico
```

### Ver reporte HTML

```bash
mvn surefire-report:report
# Abre: target/site/surefire-report.html
```

### Tests útiles que deberías escribir

```java
@Test
public void testCrearLoteConDatosValidos() {
    Lote lote = new Lote("L-001", 100, "RIR");
    assertNotNull(lote);
    assertEquals("L-001", lote.getCodigo());
}

@Test
public void testCrearLoteConCantidadNegativaLanzaExcepcion() {
    assertThrows(IllegalArgumentException.class, () -> {
        Lote lote = new Lote();
        lote.setCantidadInicial(-50);
    });
}

@Test
public void testBuscarLoteNoExistente() {
    // Debe retornar null o lanzar LoteNoEncontradoException
}

@Test
public void testCrearUsuarioConHashPassword() {
    Usuario usuario = new Usuario();
    usuario.setPasswordPlano("miPassword123");
    // El password debe estar hasheado, no en texto plano
    assertNotEquals("miPassword123", usuario.getPasswordHasheado());
}
```

---

## Compilación y Build

### Compilar sin tests

```bash
mvn clean compile
```

### Compilar + tests

```bash
mvn clean test
```

### Compilar + tests + package (JAR)

```bash
mvn clean package
```

### Limpiar archivos generados

```bash
mvn clean
```

### Saltar tests (solo en emergencias)

```bash
mvn clean compile -DskipTests
```

---

## Errores Comunes y Soluciones

### Error: "Cannot find symbol"

```
[ERROR] /path/to/Lote.java:15:5: error: cannot find symbol
```

**Causas:**
- Typo en el nombre de clase/método
- No importaste la clase
- La clase no existe aún

**Soluciones:**
```bash
# 1. Verifica spelling
# 2. Agrega import
# 3. Crea la clase
mvn clean compile  # Reintenta
```

### Error: "BUILD FAILURE - compilation errors"

```
[ERROR] BUILD FAILURE
[INFO] Total time: 1.234 s
```

**Solución:**
- Lee TODOS los errores (puede haber múltiples)
- Busca el primer `[ERROR]` - ahí empieza
- Correges uno y recompila

### Error: "Connection refused" en runtime

```
java.net.ConnectException: Connection refused
```

**Causa:** MongoDB no está corriendo

**Solución:**
```bash
# Terminal 1
mongod --dbpath "C:\data\db"

# Terminal 2
.\mongosh.exe
# Si ves ">" está OK
```

---

## Checklist de Calidad de Código

Antes de hacer commit, verifica:

- [ ] ✅ `mvn clean compile` → BUILD SUCCESS
- [ ] ✅ `mvn test` → Todos los tests pasan
- [ ] ✅ NO hay importes de Lombok
- [ ] ✅ Cada clase tiene getters/setters con validación
- [ ] ✅ Excepciones personalizadas (no `Exception` genérico)
- [ ] ✅ Naming convention: camelCase variables, PascalCase clases
- [ ] ✅ Setters validan datos
- [ ] ✅ Constructor sin args + constructor con args
- [ ] ✅ toString() implementado
- [ ] ✅ equals() y hashCode() si es entidad de dominio
- [ ] ✅ Inyección de dependencias (nunca new RepositorioXXX)
- [ ] ✅ Comentarios en métodos complejos
- [ ] ✅ No hay código comentado (bórralo)
- [ ] ✅ No hay TODO: incompletos (termina o documenta)

---

## Git Commits

**Mensajes de commit claros:**

```bash
# ✅ CORRECTO:
git commit -m "Feat: Agregar validación de cantidad en Lote"
git commit -m "Fix: Corregir hash SHA-256 en Usuario"
git commit -m "Test: Agregar casos para RepositorioLoteMongo"
git commit -m "Docs: Actualizar README.md"

# ❌ INCORRECTO:
git commit -m "cambios"
git commit -m "arreglé el bug"
git commit -m "WIP"
git commit -m "asfasdfa"
```

---

## Resumen de Reglas

| Regla | Ejemplo | Razón |
|---|---|---|
| **No Lombok** | Use getters/setters manuales | POO puro, transparencia |
| **Validación en setters** | `if (x < 0) throw Exception` | Datos siempre válidos |
| **Java Beans** | `getCodigo()`, `setCodigo()`, `isActivo()` | Convención estándar |
| **DI en constructores** | `new Servicio(repo)` | Desacoplado, testeable |
| **Excepciones personalizadas** | `LoteNoEncontradoException` | Semántica clara |
| **CamelCase variables** | `cantidadGallinas` | Estándar Java |
| **PascalCase clases** | `class Lote` | Estándar Java |
| **SCREAMING_SNAKE constantes** | `MAX_CANTIDAD` | Fácil identificar |
| **No código comentado** | Bórralo | Confunde a futuros devs |

---

**Última actualización:** Noviembre 14, 2025
