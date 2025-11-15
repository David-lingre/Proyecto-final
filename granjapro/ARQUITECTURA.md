# 🏗️ ARQUITECTURA DE GRANJAPRO

## Modelo de 4 Capas

GranjaPro implementa **Clean Architecture** organizando el código en 4 capas independientes:

```
┌─────────────────────────────────────────────────────┐
│  CAPA 1: PRESENTACIÓN (ConsolaUi)                   │
│  ─────────────────────────────────────────────────  │
│  • Interfaz de Usuario (CLI con menús ASCII)        │
│  • Manejo de input/output                           │
│  • Usa SesionGlobal para saber el usuario actual    │
│  • Muestra/oculta opciones según Rol                │
│                                                     │
│  Ubicación: presentacion/ConsolaUi.java             │
└─────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────┐
│  CAPA 2: APLICACIÓN (Servicios)                     │
│  ─────────────────────────────────────────────────  │
│  • Lógica de negocio                                │
│  • Orquestación de operaciones                      │
│  • ServicioSeguridad: login/logout/autorizaciones  │
│  • GestionLotes: crear, listar, eliminar lotes      │
│  • Produccion: registrar huevos                     │
│                                                     │
│  Ubicación: aplicacion/servicios/                   │
│             aplicacion/sesion/                      │
└─────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────┐
│  CAPA 3: DOMINIO (Entidades + Reglas)               │
│  ─────────────────────────────────────────────────  │
│  • Modelos de datos (Lote, Gallina, Usuario, etc)  │
│  • Validaciones en setters (reglas de negocio)     │
│  • Excepciones personalizadas del dominio           │
│  • Interfaces de repositorios (contratos)           │
│  • NINGUNA TECNOLOGÍA específica aquí               │
│                                                     │
│  Ubicación: dominio/modelos/                        │
│             dominio/repositorios/                   │
│             dominio/excepciones/                    │
└─────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────┐
│  CAPA 4: INFRAESTRUCTURA (MongoDB)                  │
│  ─────────────────────────────────────────────────  │
│  • Implementaciones de repositorios                 │
│  • Conexión a MongoDB                               │
│  • Mapeo Document ↔ Objetos Java                    │
│  • Queries a la base de datos                       │
│                                                     │
│  Ubicación: infraestructura/persistencia/mongo/     │
└─────────────────────────────────────────────────────┘
```

## Analogía del Restaurante 🍽️

Imagina que GranjaPro es un restaurante:

```
PRESENTACIÓN = MESERO
  ↓ Toma pedidos (entrada del usuario)
  ↓ Muestra menú (opciones según el rol)
  ↓ Entrega comida al cliente (salida)

APLICACIÓN = CHEF
  ↓ Procesa el pedido
  ↓ Verifica ingredientes disponibles
  ↓ Sigue recetas (lógica de negocio)

DOMINIO = RECETAS + INGREDIENTES
  ↓ "Una gallina necesita al menos 1 huevo al mes"
  ↓ "Un lote debe tener >= 50 gallinitas"
  ↓ Reglas que no cambian aunque cambies de restaurante

INFRAESTRUCTURA = ALMACÉN
  ↓ Guarda los ingredientes (MongoDB)
  ↓ Busca lo que el chef necesita
  ↓ Registra lo que queda
```

---

## ¿Por qué LOTE es el centro del universo y NO Gallina?

### La Verdad Agrícola 🐔

En la **realidad de una granja**:
- Un LOTE es una **unidad de gestión** (100 gallinas nacidas el mismo día)
- Una GALLINA individual es un **detalle operacional** (no nos importa la Gallina #47)

### Modelo de Datos

```
Lote (Entidad Principal)
├── id: ObjectId
├── codigo: "L-2024-001"
├── cantidad_inicial: 100
├── cantidad_actual: 95  (100 - 5 muertas)
├── raza: "RIR"
├── fecha_creacion: 2024-01-15
│
└── Producción (Derivada de Lote)
    ├── huevos_totales: 95
    ├── huevos_rotos: 5
    ├── fecha_registro: 2024-01-15
    └── calculada_del_lote
```

**NO hacemos:**
```
Gallina (Individual)
├── id: 1
├── nombre: "Cluca"
├── edad: 500 días
├── huevos_producidos: 150
├── estado_salud: "excelente"
```

### Por qué?

| Aspecto | Lote (✅) | Gallina Individual (❌) |
|---|---|---|
| **Escala** | Manejable (1-100 por registro) | Caótico (10,000+ registros) |
| **Performance** | Rápido (1 query = 1 lote) | Lento (1 query = 1 gallina) |
| **Realidad** | Así lo hace el productor | Teórico, no práctico |
| **Storage** | 1 KB por lote | 100 KB por 100 gallinas |
| **Reporte** | "Lote L-001 produjo 95 huevos" | "Cluca #47 produjo 1 huevo" |

### Conclusión

**Lote** es el **agregado raíz** (en terminología DDD - Domain Driven Design):
- Es la unidad de decisión
- Es lo que el usuario consulta
- Es lo que genera valor de negocio

Si el usuario necesita saber sobre **una gallina específica**, esa es una **nueva feature futura** (tracking individual), pero el modelo actual es correcto.

---

## Flujo de Datos

### Caso: Crear un Lote Nuevo

```
Usuario escribe: 1 (en menú)
        ↓
[PRESENTACIÓN: ConsolaUi]
- Pide: código, raza, cantidad
- Lee input
        ↓
[APLICACIÓN: GestionLotes.crearLote()]
- Valida que no exista código duplicado
- Verifica que el usuario sea ADMIN
        ↓
[DOMINIO: Lote]
- Constructor valida:
  * cantidad > 0
  * raza no vacía
  * código no vacío
- Lanza excepciones si falla
        ↓
[INFRAESTRUCTURA: RepositorioLoteMongo]
- Convierte Lote → Document
- Inserta en MongoDB
- Retorna ID generado
        ↓
[PRESENTACIÓN: ConsolaUi]
- Muestra: "✅ Lote L-001 creado con ID: 507f1f77bcf86cd799439011"
```

### Caso: Registrar Producción

```
Usuario escribe: 2 (en menú), luego: 1 (registrar producción)
        ↓
[PRESENTACIÓN: ConsolaUi]
- Pide: ID lote, huevos_totales, huevos_rotos, fecha
        ↓
[APLICACIÓN: Produccion.registrarProduccion()]
- Busca el lote en repositorio
- Si no existe → LoteNoEncontradoException
- Calcula: % válidos = (totales - rotos) / totales * 100
        ↓
[DOMINIO: RegistroDiario]
- Valida: totales >= rotos (no puede haber más rotos que totales)
        ↓
[INFRAESTRUCTURA: RepositorioLoteMongo]
- Guarda el RegistroDiario en subcampo del Lote
        ↓
[PRESENTACIÓN: ConsolaUi]
- Muestra: "✅ Registrado: 95 huevos (5 rotos = 94.7% válidos)"
```

---

## Responsabilidades de cada Capa

### PRESENTACIÓN (ConsolaUi.java)
```
✅ DEBE hacer:
- Mostrar menús ASCII
- Leer input del usuario
- Mostrar resultados

❌ NO DEBE hacer:
- Validar datos (es del DOMINIO)
- Conectarse a BD (es de INFRAESTRUCTURA)
- Hacer cálculos de negocio (es de APLICACIÓN)
```

### APLICACIÓN (GestionLotes, Produccion, ServicioSeguridad)
```
✅ DEBE hacer:
- Orquestar flujos de negocio
- Coordinar entre capas
- Autorizar operaciones (¿puede el usuario hacer esto?)

❌ NO DEBE hacer:
- Mostrar en consola (es de PRESENTACIÓN)
- Conectarse a BD (es de INFRAESTRUCTURA)
- Ser ignorante del DOMINIO (debe usar las clases de dominio)
```

### DOMINIO (Lote, Usuario, RegistroDiario)
```
✅ DEBE hacer:
- Validar en setters
- Implementar reglas de negocio
- Lanzar excepciones personalizadas
- Ser independiente de tecnologías

❌ NO DEBE hacer:
- Importar MongoDB (es INFRAESTRUCTURA)
- Mostrar en consola (es PRESENTACIÓN)
- Acceder a otras capas
```

### INFRAESTRUCTURA (RepositorioLoteMongo)
```
✅ DEBE hacer:
- Conectarse a MongoDB
- Convertir Document ↔ Objetos Java
- Implementar operaciones de BD

❌ NO DEBE hacer:
- Validar (es del DOMINIO)
- Mostrar en consola (es de PRESENTACIÓN)
- Saber de lógica de negocio (es de APLICACIÓN)
```

---

## Patrones Implementados

### 1. Repository Pattern
Abstrae la persistencia detrás de interfaces:

```
RepositorioLote (Interface - DOMINIO)
    ↑
    └── RepositorioLoteMongo (Implementación - INFRAESTRUCTURA)
    └── RepositorioLoteMemoria (Futura - para testing)
    └── RepositorioLoteSQL (Futura - si cambias de BD)
```

**Ventaja:** Cambiar de BD es cambiar 1 clase, no 50.

### 2. Dependency Injection
Las dependencias se pasan por constructor:

```java
// ✅ CORRECTO:
public GestionLotes(RepositorioLote repositorio) {
    this.repositorio = repositorio;  // Inyectada
}

// ❌ INCORRECTO:
public GestionLotes() {
    this.repositorio = new RepositorioLoteMongo();  // Acoplada
}
```

### 3. Singleton (SesionGlobal)
Un único objeto "usuario logueado" accesible desde cualquier parte:

```java
// Desde ConsolaUi:
if (SesionGlobal.get().esAdmin()) {
    mostrarOpcionBorrarLote();
}

// Desde ServicioSeguridad:
SesionGlobal.get().iniciarSesion(usuario);

// El usuario está en RAM durante la ejecución
```

### 4. Strategy (Rol Enum)
Define comportamientos diferentes según el rol:

```java
enum Rol {
    ADMIN("Acceso completo"),
    OPERARIO("Acceso limitado");
    
    public boolean esAdmin() { return this == ADMIN; }
}

// Uso:
if (usuario.getRol().esAdmin()) {
    // Permitir borrar lote
}
```

---

## ¿Cómo se Conectan las Capas?

### Ejemplo: Crear un Lote

```
ConsolaUi
  ↓ (pide datos)
GestionLotes (recibe datos)
  ↓ (necesita guardar)
RepositorioLote (interfaz, pide guardar)
  ↓ (implementación concreta)
RepositorioLoteMongo (conecta a MongoDB)
  ↓ (convierte Lote → Document)
MongoDB
  ↓ (retorna ID)
RepositorioLoteMongo (retorna ID)
  ↓
GestionLotes (retorna Lote guardado)
  ↓
ConsolaUi (muestra resultado)
```

### El Triángulo de Validación

Hay 3 puntos donde validamos:

```
1. PRESENTACIÓN (ConsolaUi)
   └─ Valida: "¿El usuario escribió algo?"
   └─ Ejemplo: if (codigo.isBlank()) { error }

2. DOMINIO (Lote setter)
   └─ Valida: "¿Esto tiene sentido según reglas de negocio?"
   └─ Ejemplo: if (cantidad <= 0) { excepción }

3. INFRAESTRUCTURA (RepositorioLoteMongo)
   └─ Valida: "¿MongoDB permite esto?"
   └─ Ejemplo: if (ya_existe_codigo) { excepción }
```

---

## Decisiones de Diseño Importantes

### ❌ NO usamos Lombok

**Razón:** Este es un curso de POO puro. Lombok oculta la abstracción.

**Regla:**
```java
// ✅ CORRECTO:
public String getCodigo() { return this.codigo; }
public void setCodigo(String codigo) {
    if (codigo == null || codigo.isBlank()) 
        throw new DatoInvalidoException("Código no puede estar vacío");
    this.codigo = codigo;
}

// ❌ INCORRECTO:
@Getter
@Setter
private String codigo;
```

### ⭐ Seguridad con Roles

**Idea:** La vista (ConsolaUi) NO hace la validación de roles. La servicio lo hace.

```
ConsolaUi (presentación)
  └─ IF usuario.esAdmin() THEN mostrar_opción
  └─ Pero no decide si está permitido
  └─ Solo decide qué mostrar

GestionLotes (servicio, lógica real)
  └─ if (!usuario.esAdmin()) throw new Exception()
  └─ La lógica real está acá
```

**Resultado:** Si alguien hackea la UI (quita el IF), el servidor sigue rechazando.

### 🗄️ MongoDB como Agregador

Cada documento MongoDB representa un **Lote COMPLETO**:

```json
{
  "_id": ObjectId("507f1f77bcf86cd799439011"),
  "codigo": "L-2024-001",
  "raza": "RIR",
  "cantidad_inicial": 100,
  "cantidad_actual": 95,
  "registros_produccion": [
    {
      "fecha": "2024-01-15",
      "huevos_totales": 95,
      "huevos_rotos": 5
    },
    {
      "fecha": "2024-01-16",
      "huevos_totales": 94,
      "huevos_rotos": 3
    }
  ]
}
```

**Ventaja:** 1 lote = 1 documento = 1 query = Rápido

---

## Extensibilidad

Si mañana necesitas:

### ✅ Cambiar de BD (MongoDB → SQL)
```
Crear: RepositorioLoteSQL implements RepositorioLote
Cambiar en ConexionMongo/Factory el nuevo repo
LISTO - El resto del código no cambia
```

### ✅ Agregar email de notificaciones
```
Crear: NotificadorEmail (nuevo servicio)
Inyectar en GestionLotes
Llamar cuando se crea un lote
El resto del código no se toca
```

### ✅ Agregar nuevo rol (SUPERVISOR)
```
Agregar: SUPERVISOR("Lectura solamente") en Rol enum
Actualizar if statements en ConsolaUi
LISTO
```

---

## Resumen

**Clean Architecture permite que cada capa:**
- Sea independiente
- Sea reemplazable
- Sea testeable
- Sea mantenible

**Las 4 capas fluyen así:**
```
PRESENTACIÓN ←→ APLICACIÓN ←→ DOMINIO ←→ INFRAESTRUCTURA
(User input)     (Orquestación) (Reglas)    (Persistencia)
```

**El núcleo (DOMINIO) es el más estable**, las capas externas cambian con tecnologías.

**Última actualización:** Noviembre 14, 2025
