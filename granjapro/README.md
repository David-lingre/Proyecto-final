# 🐔 GranjaPro - Sistema de Gestión Avícola

**Versión:** 1.0  
**Estado:** ✅ Producción  
**Última actualización:** Noviembre 14, 2025

---

## 📖 Descripción General

**GranjaPro** es un sistema de gestión integral para granjas avícolas, diseñado siguiendo los principios de **Clean Architecture** con tecnologías modernas. Permite administrar lotes de gallinas, registrar producción de huevos, monitorear indicadores clave de producción y **controlar acceso mediante autenticación RBAC**.

**Tecnologías:**
- ✅ **Java 17+** (sin Lombok - POO puro)
- ✅ **Clean Architecture** (4 capas bien definidas)
- ✅ **MongoDB** (persistencia NoSQL)
- ✅ **Maven** (gestión de dependencias)
- ✅ **CLI** (interfaz de consola profesional)
- ✅ **Seguridad RBAC** (Roles: ADMIN/OPERARIO, SHA-256 hashing)

---

## 🎯 Características Principales

```
✅ Autenticación y Seguridad (NUEVO)
   └─ Login con Usuario/Contraseña
   └─ Roles: ADMIN (acceso completo) / OPERARIO (acceso limitado)
   └─ Contraseñas hasheadas con SHA-256
   └─ Sesión en memoria (SesionGlobal Singleton)

✅ Gestión de Lotes
   └─ Crear lotes de gallinas
   └─ Registrar mortalidad en tiempo real
   └─ Visualizar detalles y listados
   └─ ADMIN SOLO: Eliminar/modificar lotes

✅ Registro de Producción
   └─ Registrar producción de huevos
   └─ Calcular porcentajes de roturas
   └─ Análisis de calidad

✅ Persistencia
   └─ Base de datos MongoDB
   └─ Mapeo automático POJO ↔ Document
   └─ Consultas eficientes

✅ Validación de Datos
   └─ Excepciones personalizadas
   └─ Validación en setters
   └─ Mensajes de error profesionales
```

---

## ⚙️ Requisitos Previos

| Componente | Versión | Verificación |
|---|---|---|
| **Java JDK** | 17 o superior | `java -version` |
| **Maven** | 3.8.1 o superior | `mvn -version` |
| **MongoDB** | 4.4 o superior | Ver sección MongoDB |

### Verificar Java 17+
```powershell
java -version
# Debe mostrar: openjdk version "17.x.x" o superior
```

### Verificar Maven
```powershell
mvn -version
# Debe mostrar: Apache Maven 3.8.x o superior
```

---

## 🗄️ CRÍTICO: Configuración de MongoDB

**⚠️ MongoDB DEBE estar corriendo antes de ejecutar GranjaPro**

### Opción 1: MongoDB Local (Windows)

**Paso 1:** Descargar MongoDB Community Edition
```
Ir a: https://www.mongodb.com/try/download/community
Descargar el instalador .msi (Windows)
```

**Paso 2:** Instalar MongoDB
- Ejecutar el instalador `.msi`
- Siguiente → Siguiente → Instalar

**Paso 3:** Iniciar MongoDB (PowerShell como Administrador)
```powershell
# Generalmente en:
cd "C:\Program Files\MongoDB\Server\7.0\bin"

# Iniciar el servidor:
.\mongod.exe

# Deberías ver:
# [connection] connection accepted from 127.0.0.1:XXXXX
# Ready to accept connections
```

**Paso 4:** ✅ Verificar que corre
```powershell
# En OTRA terminal PowerShell:
cd "C:\Program Files\MongoDB\Server\7.0\bin"
.\mongosh.exe

# Deberías ver el prompt:
# >
# Escribe: exit
```

### Opción 2: MongoDB con Docker (Recomendado)

```powershell
# Si tienes Docker instalado:
docker run -d -p 27017:27017 --name granja-mongodb mongo:4.4

# Verificar que corre:
docker ps | findstr mongodb

# Para detener:
docker stop granja-mongodb
```

### Opción 3: MongoDB Atlas (Cloud)

**Paso 1:** Crear cuenta en https://www.mongodb.com/cloud/atlas

**Paso 2:** Crear un cluster (tarda 2-3 min)

**Paso 3:** Obtener conexión
- Ir a: Database → Clusters → Connect
- Copiar la cadena de conexión

**Paso 4:** Actualizar ConexionMongo.java
```java
// Archivo: src/main/java/com/granjapro/infraestructura/persistencia/mongo/ConexionMongo.java

// Cambiar línea 15:
private static final String MONGO_URI = "mongodb://localhost:27017";

// Por tu Atlas URL:
private static final String MONGO_URI = "mongodb+srv://usuario:pass@cluster.mongodb.net";
```

---

## 🚀 Quickstart (5 minutos)

### Paso 1: Clonar el repositorio
```powershell
git clone https://github.com/UAN-POO/proyectos-gargolas.git
cd proyectos-gargolas/Corte4/granjapro
```

### Paso 2: Compilar
```powershell
mvn clean compile
```

Esperado:
```
[INFO] Compiling 19 source files...
[INFO] BUILD SUCCESS
[INFO] Total time: ~15 seconds
```

### Paso 3: ⚠️ Asegurar que MongoDB corre
```powershell
# Terminal 1: Levanta MongoDB
.\mongod.exe    # O: docker run -d -p 27017:27017 mongo:4.4

# Terminal 2: Verifica conexión
.\mongosh.exe
# Si ves el prompt ">" → MongoDB está OK
# Escribe: exit
```

### Paso 4: Ejecutar GranjaPro
```powershell
# Opción A (recomendada):
mvn exec:java -Dexec.mainClass="com.granjapro.presentacion.ConsolaUi"

# Opción B:
java -cp target/classes com.granjapro.presentacion.ConsolaUi
```

### Paso 5: Login
```
Usuario: admin
Contraseña: admin123

✅ Bienvenido, admin (Rol: ADMIN)
```

---

---

## 📁 Estructura del Proyecto

```
granjapro/
│
├── 📄 pom.xml                              (Configuración Maven)
├── 📄 README.md                            (Este archivo)
├── 📄 ARQUITECTURA.md                      (Diseño de 4 capas)
├── 📄 MANUAL_GENERAL.md                    (Estándares de código)
├── 📄 TAREA_IVAN_EXCEPCIONES.md            (Para Iván)
├── 📄 TAREA_DAVID_SEGURIDAD.md             (Para David)
│
└── src/
    ├── main/java/com/granjapro/
    │   ├── Main.java                       (Punto de entrada)
    │   │
    │   ├── presentacion/
    │   │   └── ConsolaUi.java              (Interfaz CLI con Login)
    │   │
    │   ├── aplicacion/
    │   │   ├── servicios/
    │   │   │   ├── GestionLotes.java       (Lógica de negocio)
    │   │   │   ├── Produccion.java         (Lógica de producción)
    │   │   │   └── ServicioSeguridad.java  (⭐ Nuevo - Auth)
    │   │   │
    │   │   └── sesion/
    │   │       └── SesionGlobal.java       (⭐ Nuevo - Singleton usuario)
    │   │
    │   ├── dominio/
    │   │   ├── modelos/
    │   │   │   ├── Lote.java               (Entidad principal)
    │   │   │   ├── Gallina.java
    │   │   │   ├── Huevo.java
    │   │   │   ├── RegistroDiario.java
    │   │   │   ├── Usuario.java            (⭐ Nuevo - Entidad con SHA-256)
    │   │   │   └── Rol.java                (⭐ Nuevo - Enum ADMIN/OPERARIO)
    │   │   │
    │   │   ├── repositorios/
    │   │   │   ├── RepositorioLote.java
    │   │   │   ├── RepositorioGallina.java
    │   │   │   ├── RepositorioHuevo.java
    │   │   │   └── RepositorioUsuario.java (⭐ Nuevo - Interface)
    │   │   │
    │   │   └── excepciones/
    │   │       ├── GranjaException.java    (Base)
    │   │       └── [Otras excepciones - TAREA IVÁN]
    │   │
    │   └── infraestructura/
    │       ├── ConexionMongo.java          (Singleton conexión)
    │       └── persistencia/mongo/
    │           ├── RepositorioLoteMongo.java
    │           ├── RepositorioGallinaMongo.java
    │           ├── RepositorioHuesoMongo.java
    │           └── RepositorioUsuarioMongo.java (⭐ Nuevo - Impl MongoDB)
    │
    └── test/java/com/granjapro/
        ├── AppTest.java
        └── service/
            └── OperacionesServiceTest.java
```

---

## 🧪 Ejecutar Tests

```powershell
# Ejecutar todos los tests
mvn test

# Ejecutar un test específico
mvn test -Dtest=NombreDelTest

# Ejecutar tests con reporte
mvn surefire-report:report
```

---

## 🏗️ Arquitectura

GranjaPro implementa **Clean Architecture** en 4 capas independientes:

```
┌─────────────────────────────────────────┐
│  PRESENTACIÓN (ConsolaUi)               │
│  └─ Interfaz CLI con menús ASCII        │
├─────────────────────────────────────────┤
│  APLICACIÓN (Servicios)                 │
│  └─ Lógica de negocio                   │
├─────────────────────────────────────────┤
│  DOMINIO (Entidades + Excepciones)      │
│  └─ Reglas del negocio                  │
├─────────────────────────────────────────┤
│  INFRAESTRUCTURA (MongoDB)              │
│  └─ Persistencia de datos               │
└─────────────────────────────────────────┘
```

**Beneficios:**
- ✅ Independencia de tecnologías
- ✅ Fácil testing
- ✅ Código mantenible
- ✅ Escalabilidad

Más detalles en: **ARQUITECTURA_DEL_SISTEMA.md**

---

## 💻 Operaciones Principales

### Gestión de Lotes
```
✅ Crear un lote
   Input: código, raza, cantidad inicial, ID corral
   Output: Lote guardado en MongoDB

✅ Registrar mortalidad
   Input: ID lote, cantidad muertas
   Output: Cantidad actualizada

✅ Ver todos los lotes
   Output: Listado completo

✅ Ver detalles
   Input: ID lote
   Output: Información detallada
```

### Registro de Producción
```
✅ Registrar producción
   Input: ID lote, huevos totales, huevos rotos, fecha
   Output: Registro guardado

✅ Ver registros
   Input: ID lote
   Output: Historial de producción

✅ Calcular calidad
   Output: Porcentaje de huevos válidos
```

---

## 🔍 Validación y Excepciones

GranjaPro valida todos los datos antes de guardarlos:

```java
// Las excepciones son específicas al dominio
try {
    Lote lote = new Lote("L-001", "RIR", 100, "CORRAL-A");
    // Si hay error, lanza excepción semántica
    // NO genérico IllegalArgumentException
} catch (CantidadInvalidaException e) {
    System.out.println("❌ " + e.getMessage());
}
```

**Excepciones personalizadas:**
- `GranjaException` (base)
- `LoteNoEncontradoException`
- `CantidadInvalidaException`
- `DatoInvalidoException`
- `ProduccionInvalidaException`

---

## 🚨 Reglas Importantes

### ❌ NUNCA usar Lombok
```java
// ❌ INCORRECTO:
@Data
@AllArgsConstructor
public class Lote { }

// ✅ CORRECTO:
public class Lote {
    private String codigo;
    
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
}
```

### ✅ Siempre validar en setters
```java
public void setCantidadInicial(Integer cantidad) {
    if (cantidad <= 0) {
        throw new CantidadInvalidaException("Cantidad debe ser > 0");
    }
    this.cantidadInicial = cantidad;
}
```

### ✅ Usar inyección de dependencias
```java
// En servicios
public ServicioGestionLotes(RepositorioLote repositorio) {
    this.repositorio = repositorio;
}
```

---

## 📝 Ejemplos de Uso

### Crear un lote
```powershell
Menu → 1 (Gestión de Lotes)
Submenu → 1 (Crear nuevo lote)

Ingresa código: L-2024-001
Ingresa raza: RIR
Ingresa cantidad: 100
Ingresa corral: CORRAL-A

✅ Lote creado exitosamente
```

### Registrar producción
```powershell
Menu → 2 (Producción)
Submenu → 1 (Registrar producción)

Ingresa ID lote: 507f1f77bcf86cd799439011
Ingresa huevos totales: 200
Ingresa huevos rotos: 10
Ingresa fecha: 2024-01-15

✅ Producción registrada
```

---

## 🐛 Troubleshooting

### Error: "MongoDB connection refused"
```
❌ Problema: MongoDB no está corriendo
✅ Solución:
   1. Inicia MongoDB: .\mongod.exe
   2. Verifica que escuche en localhost:27017
   3. Intenta de nuevo
```

### Error: "Could not find main class"
```
❌ Problema: ConsolaUi no es accesible
✅ Solución:
   1. Compila: mvn clean compile
   2. Ejecuta: mvn exec:java -Dexec.mainClass="com.granjapro.presentacion.ConsolaUi"
```

### Error: "BUILD FAILURE"
```
❌ Problema: Error de compilación
✅ Solución:
   1. Lee el error completo
   2. Busca problemas de importación
   3. Verifica que no tengas Lombok
   4. Intenta: mvn clean compile
```

---

## 📚 Documentación Relacionada

- **ARQUITECTURA_DEL_SISTEMA.md** - Explicación técnica detallada
- **MANUAL_DESARROLLADOR.md** - Guía para el equipo de desarrollo

---

## 🤝 Equipo

- **Arquitécto:** Sistema diseñado bajo Clean Architecture
- **Lead Técnico:** Responsable de integración
- **Desarrolladores:** Iván (Excepciones), David (CLI)

---

## 📞 Soporte

Para preguntas sobre el proyecto:
1. Consulta **ARQUITECTURA_DEL_SISTEMA.md** (diseño)
2. Consulta **MANUAL_DESARROLLADOR.md** (desarrollo)
3. Revisa ejemplos en la sección "Ejemplos de Uso"

---

## ✅ Checklist de Inicio

- [ ] Java 17+ instalado (`java -version`)
- [ ] Maven instalado (`mvn -version`)
- [ ] MongoDB corriendo (`.\mongod.exe`)
- [ ] Proyecto clonado
- [ ] `mvn clean compile` exitoso
- [ ] `mvn exec:java -Dexec.mainClass="com.granjapro.presentacion.ConsolaUi"` funcionando
- [ ] Menú visible en consola

---

**Status:** ✅ Listo para producción  
**Fecha:** Noviembre 2024  
**Versión:** 1.0
