# 🎯 TAREA_IVAN_EXCEPCIONES - Para Iván

## Misión

Iván, tu responsabilidad es **crear todas las excepciones personalizadas** del dominio. Actualmente solo existe `GranjaException` (la base). Necesitamos que crees 5 excepciones más y luego refactorices `Lote.java` para usarlas.

**Estimación:** 45 minutos  
**Dificultad:** Media (es principalmente copy-paste + entendimiento)  
**Fecha entrega:** Antes de que David termine integración (90 minutos desde ahora)

---

## Paso 1: Ver la Excepción Base

Lee el archivo actual:

📁 **Ubicación:** `src/main/java/com/granjapro/dominio/excepciones/GranjaException.java`

```java
public class GranjaException extends Exception {
    public GranjaException(String mensaje) {
        super(mensaje);
    }
    
    public GranjaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
```

**Esto es correcto.** No lo toques. Es tu base.

---

## Paso 2: Crear las 5 Excepciones Nuevas

Crea estos archivos en: `src/main/java/com/granjapro/dominio/excepciones/`

### Excepción 1: LoteNoEncontradoException

**Archivo:** `LoteNoEncontradoException.java`

```java
package com.granjapro.dominio.excepciones;

/**
 * Se lanza cuando se busca un Lote que no existe en la BD.
 * Ej: "Lote con ID 507f... no encontrado"
 */
public class LoteNoEncontradoException extends GranjaException {
    public LoteNoEncontradoException(String id) {
        super("Lote con ID '" + id + "' no encontrado en la base de datos");
    }
    
    public LoteNoEncontradoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
```

**Cuándo se lanza:**
```java
// En RepositorioLoteMongo.java:
Lote lote = coleccion.find(...).first();
if (lote == null) {
    throw new LoteNoEncontradoException(id);  // ← Aquí
}
```

---

### Excepción 2: CantidadInvalidaException

**Archivo:** `CantidadInvalidaException.java`

```java
package com.granjapro.dominio.excepciones;

/**
 * Se lanza cuando la cantidad de gallinas es inválida.
 * Ej: cantidad <= 0, cantidad > 10000, cantidad_actual > cantidad_inicial
 */
public class CantidadInvalidaException extends GranjaException {
    public CantidadInvalidaException(String mensaje) {
        super(mensaje);
    }
    
    public CantidadInvalidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
    
    // Constructor de conveniencia para cantidad inválida
    public static CantidadInvalidaException cantidadNegativaOCero(Integer cantidad) {
        return new CantidadInvalidaException(
            "Cantidad debe ser mayor a 0. Recibiste: " + cantidad
        );
    }
    
    public static CantidadInvalidaException cantidadMayorAlMaximo(Integer cantidad, Integer maximo) {
        return new CantidadInvalidaException(
            "Cantidad (" + cantidad + ") excede máximo permitido (" + maximo + ")"
        );
    }
}
```

**Cuándo se lanza:**
```java
// En Lote.java setter:
public void setCantidadInicial(Integer cantidad) {
    if (cantidad == null || cantidad <= 0) {
        throw CantidadInvalidaException.cantidadNegativaOCero(cantidad);  // ← Aquí
    }
    if (cantidad > 10000) {
        throw CantidadInvalidaException.cantidadMayorAlMaximo(cantidad, 10000);
    }
    this.cantidadInicial = cantidad;
}
```

---

### Excepción 3: DatoInvalidoException

**Archivo:** `DatoInvalidoException.java`

```java
package com.granjapro.dominio.excepciones;

/**
 * Se lanza cuando un dato es inválido (vacío, null, formato incorrecto).
 * Usada para: código, raza, nombre, etc.
 */
public class DatoInvalidoException extends GranjaException {
    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
    
    public DatoInvalidoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
    
    // Constructores de conveniencia
    public static DatoInvalidoException campoVacio(String nombreCampo) {
        return new DatoInvalidoException(
            "El campo '" + nombreCampo + "' no puede estar vacío"
        );
    }
    
    public static DatoInvalidoException campoNull(String nombreCampo) {
        return new DatoInvalidoException(
            "El campo '" + nombreCampo + "' no puede ser null"
        );
    }
    
    public static DatoInvalidoException longitudInsuficiente(String nombreCampo, Integer minimo) {
        return new DatoInvalidoException(
            "El campo '" + nombreCampo + "' debe tener al menos " + minimo + " caracteres"
        );
    }
}
```

**Cuándo se lanza:**
```java
// En Lote.java setter:
public void setCodigo(String codigo) {
    if (codigo == null) {
        throw DatoInvalidoException.campoNull("codigo");  // ← Aquí
    }
    if (codigo.isBlank()) {
        throw DatoInvalidoException.campoVacio("codigo");  // ← Aquí
    }
    this.codigo = codigo;
}

public void setRaza(String raza) {
    if (raza == null || raza.length() < 2) {
        throw DatoInvalidoException.longitudInsuficiente("raza", 2);  // ← Aquí
    }
    this.raza = raza;
}
```

---

### Excepción 4: ProduccionInvalidaException

**Archivo:** `ProduccionInvalidaException.java`

```java
package com.granjapro.dominio.excepciones;

/**
 * Se lanza cuando un registro de producción es inválido.
 * Ej: huevos_rotos > huevos_totales, sin lote asociado, fecha inválida
 */
public class ProduccionInvalidaException extends GranjaException {
    public ProduccionInvalidaException(String mensaje) {
        super(mensaje);
    }
    
    public ProduccionInvalidaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
    
    // Constructores de conveniencia
    public static ProduccionInvalidaException rostosMayorQueTotales(
            Integer totales, Integer rotos) {
        return new ProduccionInvalidaException(
            "Huevos rotos (" + rotos + ") no puede exceder totales (" + totales + ")"
        );
    }
    
    public static ProduccionInvalidaException sinLoteAsociado(String loteid) {
        return new ProduccionInvalidaException(
            "No hay lote con ID " + loteid + " para registrar producción"
        );
    }
}
```

**Cuándo se lanza:**
```java
// En Produccion.java o RegistroDiario.java:
if (huesoRotos > huesoTotales) {
    throw ProduccionInvalidaException.rostosMayorQueTotales(
        huesoTotales, huesoRotos
    );  // ← Aquí
}
```

---

### Excepción 5: UsuarioNoAutorizadoException

**Archivo:** `UsuarioNoAutorizadoException.java`

```java
package com.granjapro.dominio.excepciones;

/**
 * Se lanza cuando un usuario intenta hacer algo que su rol no permite.
 * Ej: OPERARIO intentando borrar un lote (solo ADMIN puede)
 */
public class UsuarioNoAutorizadoException extends GranjaException {
    public UsuarioNoAutorizadoException(String usuario, String accion, String rolRequerido) {
        super("Usuario '" + usuario + "' no tiene permisos para '" + accion 
              + "'. Se requiere rol: " + rolRequerido);
    }
    
    public UsuarioNoAutorizadoException(String mensaje) {
        super(mensaje);
    }
    
    public UsuarioNoAutorizadoException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
```

**Cuándo se lanza:**
```java
// En GestionLotes.java:
public void eliminarLote(String id) throws GranjaException {
    Usuario usuarioActual = SesionGlobal.get().obtenerUsuario();
    if (!usuarioActual.esAdmin()) {
        throw new UsuarioNoAutorizadoException(
            usuarioActual.getNombre(),
            "eliminarLote",
            "ADMIN"
        );  // ← Aquí
    }
    repositorio.eliminar(id);
}
```

---

## Paso 3: Refactorizar Lote.java

Ahora que tienes las excepciones, **modifica `Lote.java`** para usarlas.

### Ubicación Actual
`src/main/java/com/granjapro/dominio/modelos/Lote.java`

### Cambios a Realizar

#### CAMBIO 1: Agregar imports

**Busca:** La sección de imports (primeras líneas)

**Reemplaza:**
```java
package com.granjapro.dominio.modelos;

// Agregar estos imports:
import com.granjapro.dominio.excepciones.CantidadInvalidaException;
import com.granjapro.dominio.excepciones.DatoInvalidoException;
```

#### CAMBIO 2: Modificar setCodigo()

**Busca en Lote.java:**
```java
public void setCodigo(String codigo) {
    if (codigo == null || codigo.isBlank()) {
        throw new IllegalArgumentException("Código no puede estar vacío");
    }
    this.codigo = codigo;
}
```

**Reemplaza con:**
```java
public void setCodigo(String codigo) {
    if (codigo == null) {
        throw DatoInvalidoException.campoNull("codigo");
    }
    if (codigo.isBlank()) {
        throw DatoInvalidoException.campoVacio("codigo");
    }
    this.codigo = codigo;
}
```

#### CAMBIO 3: Modificar setCantidadInicial()

**Busca en Lote.java:**
```java
public void setCantidadInicial(Integer cantidad) {
    if (cantidad == null || cantidad <= 0) {
        throw new IllegalArgumentException("Cantidad debe ser > 0");
    }
    this.cantidadInicial = cantidad;
}
```

**Reemplaza con:**
```java
public void setCantidadInicial(Integer cantidad) {
    if (cantidad == null) {
        throw DatoInvalidoException.campoNull("cantidadInicial");
    }
    if (cantidad <= 0) {
        throw CantidadInvalidaException.cantidadNegativaOCero(cantidad);
    }
    if (cantidad > 10000) {
        throw CantidadInvalidaException.cantidadMayorAlMaximo(cantidad, 10000);
    }
    this.cantidadInicial = cantidad;
}
```

#### CAMBIO 4: Modificar setRaza()

**Busca en Lote.java:**
```java
public void setRaza(String raza) {
    if (raza == null || raza.isBlank()) {
        throw new IllegalArgumentException("Raza no puede estar vacía");
    }
    this.raza = raza;
}
```

**Reemplaza con:**
```java
public void setRaza(String raza) {
    if (raza == null) {
        throw DatoInvalidoException.campoNull("raza");
    }
    if (raza.isBlank()) {
        throw DatoInvalidoException.campoVacio("raza");
    }
    if (raza.length() < 2) {
        throw DatoInvalidoException.longitudInsuficiente("raza", 2);
    }
    this.raza = raza;
}
```

#### CAMBIO 5: (OPCIONAL) Modificar otros setters

Si hay más setters en Lote, aplica el mismo patrón. Ejemplos:

```java
// setCantidadActual() - debe ser <= cantidadInicial
public void setCantidadActual(Integer cantidad) {
    if (cantidad == null) {
        throw DatoInvalidoException.campoNull("cantidadActual");
    }
    if (cantidad < 0) {
        throw CantidadInvalidaException.cantidadNegativaOCero(cantidad);
    }
    if (cantidad > this.cantidadInicial) {
        throw new CantidadInvalidaException(
            "Cantidad actual (" + cantidad + ") no puede ser > inicial (" + this.cantidadInicial + ")"
        );
    }
    this.cantidadActual = cantidad;
}
```

---

## Paso 4: Verificar Compilación

Una vez hayas creado las 5 excepciones y refactorizado Lote.java:

```bash
mvn clean compile
```

**Resultado esperado:**
```
[INFO] Compiling 19+ source files...
[INFO] BUILD SUCCESS
[INFO] Total time: ~15 seconds
```

Si hay errores:
1. Lee el mensaje de error (dónde exactamente falla)
2. Verifica que los imports estén correctos
3. Verifica que nombres de excepciones sean exactos
4. Reintenta: `mvn clean compile`

---

## Paso 5: Documentación

Agrega comentarios JavaDoc a cada excepción. Ejemplo:

```java
/**
 * Se lanza cuando un Lote no existe en la base de datos.
 * 
 * Ejemplos de uso:
 * <pre>
 *     Lote lote = repositorio.buscarPorId("invalid-id");
 *     if (lote == null) {
 *         throw new LoteNoEncontradoException("invalid-id");
 *     }
 * </pre>
 * 
 * @author Iván
 * @version 1.0
 */
public class LoteNoEncontradoException extends GranjaException {
    // ...
}
```

---

## Checklist de Iván

- [ ] ✅ Creé `LoteNoEncontradoException.java`
- [ ] ✅ Creé `CantidadInvalidaException.java`
- [ ] ✅ Creé `DatoInvalidoException.java`
- [ ] ✅ Creé `ProduccionInvalidaException.java`
- [ ] ✅ Creé `UsuarioNoAutorizadoException.java`
- [ ] ✅ Agregué imports a Lote.java
- [ ] ✅ Modifiqué setCodigo() en Lote.java
- [ ] ✅ Modifiqué setCantidadInicial() en Lote.java
- [ ] ✅ Modifiqué setRaza() en Lote.java
- [ ] ✅ `mvn clean compile` → BUILD SUCCESS
- [ ] ✅ Sin errores de compilación
- [ ] ✅ Agregué comentarios JavaDoc

---

## Próximos Pasos Después de Terminar

Una vez termines:

1. Haz git commit:
```bash
git add src/main/java/com/granjapro/dominio/
git commit -m "Feat: Crear 5 excepciones personalizadas del dominio (Iván)"
```

2. Avisa al Lead: "Las excepciones están listas"

3. (Opcional) Refactoriza otras clases si usan `IllegalArgumentException` o `Exception` genérico:
   - `Gallina.java`
   - `Huevo.java`
   - `RegistroDiario.java`

---

## Referencias

- **Leer primero:** `MANUAL_GENERAL.md` (sección "Estructura de Excepciones")
- **Arquitectura:** `ARQUITECTURA.md` (sección "Responsabilidades de cada Capa")
- **Base class:** `src/main/java/com/granjapro/dominio/excepciones/GranjaException.java`

---

## ¿Preguntas?

Si algo no está claro:
1. Consulta `MANUAL_GENERAL.md`
2. Mira ejemplos en este documento
3. Pregunta al Lead

**Éxito, Iván. Esto es fundamental para que David pueda trabajar sin problemas.** 🚀

---

**Fecha creación:** Noviembre 14, 2025  
**Estimación:** 45 minutos  
**Prioridad:** ALTA
