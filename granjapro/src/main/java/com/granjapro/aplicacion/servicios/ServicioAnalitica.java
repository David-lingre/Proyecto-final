package com.granjapro.aplicacion.servicios;

import com.granjapro.dominio.modelos.Alerta;
import com.granjapro.dominio.modelos.Alerta.TipoAlerta;
import com.granjapro.dominio.modelos.Lote;
import com.granjapro.dominio.modelos.RegistroProduccion;
import com.granjapro.dominio.repositorios.RepositorioAlerta;
import com.granjapro.dominio.repositorios.RepositorioLote;
import com.granjapro.dominio.repositorios.RepositorioRegistroProduccion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de Analítica que implementa la lógica de negocio inteligente.
 * 
 * Responsabilidades:
 * 1. Detectar automáticamente problemas en la producción (Alertas Inteligentes)
 * 2. Calcular KPIs técnicos (Tasa de Postura, ICA, etc.)
 * 3. Generar reportes resumidos para toma de decisiones
 * 
 * Utiliza patrón Dependency Injection para acceder a los datos.
 * Clean Architecture: Capa de Aplicación (Servicios)
 */
public class ServicioAnalitica {
    
    private final RepositorioLote repositorioLote;
    private final RepositorioRegistroProduccion repositorioProduccion;
    private final RepositorioAlerta repositorioAlerta;
    
    /**
     * Constructor que inyecta las dependencias necesarias.
     * @param repositorioLote Para obtener datos del lote
     * @param repositorioProduccion Para obtener registros de producción del día
     * @param repositorioAlerta Para guardar las alertas detectadas
     */
    public ServicioAnalitica(
            RepositorioLote repositorioLote,
            RepositorioRegistroProduccion repositorioProduccion,
            RepositorioAlerta repositorioAlerta) {
        
        if (repositorioLote == null) {
            throw new IllegalArgumentException("Repositorio de lotes no puede ser nulo");
        }
        if (repositorioProduccion == null) {
            throw new IllegalArgumentException("Repositorio de producción no puede ser nulo");
        }
        if (repositorioAlerta == null) {
            throw new IllegalArgumentException("Repositorio de alertas no puede ser nulo");
        }
        
        this.repositorioLote = repositorioLote;
        this.repositorioProduccion = repositorioProduccion;
        this.repositorioAlerta = repositorioAlerta;
    }
    
    // ===================== TAREA C: SISTEMA DE ALERTAS INTELIGENTES =====================
    
    /**
     * MÉTODO PRINCIPAL: Ejecuta el análisis diario automático de un lote.
     * 
     * Algoritmo:
     * 1. Obtiene el lote y sus datos actuales
     * 2. Busca registros de producción de hoy
     * 3. Calcula Tasa de Postura del día
     * 4. Genera alertas según reglas de negocio:
     *    - ADVERTENCIA si Tasa < 70%
     *    - INFO si hubo mortalidad
     * 
     * Esta es la función que se llamará automáticamente cada madrugada,
     * o manualmente desde el menú cuando el operario lo solicite.
     * 
     * @param idLote Identificador del lote a analizar
     */
    public void ejecutarAnalisisDiario(String idLote) {
        if (idLote == null || idLote.isBlank()) {
            throw new IllegalArgumentException("El ID del lote no puede estar vacío");
        }
        
        // Paso 1: Obtener el lote
        Optional<Lote> optLote = repositorioLote.buscarPorId(idLote);
        if (optLote.isEmpty()) {
            throw new IllegalArgumentException("Lote no encontrado: " + idLote);
        }
        Lote lote = optLote.get();
        
        // Paso 2: Obtener registros de producción de hoy (del mismo lote)
        LocalDate hoy = LocalDate.now();
        List<RegistroProduccion> registrosHoy = 
            repositorioProduccion.buscarPorIdLote(idLote);
        
        // Filtrar solo los registros de hoy
        registrosHoy = registrosHoy.stream()
            .filter(r -> r.getFecha().equals(hoy))
            .toList();
        
        if (registrosHoy.isEmpty()) {
            // Sin registros de hoy: no hay análisis que hacer
            return;
        }
        
        // Paso 3: Calcular Tasa de Postura del día
        int totalHuevosTotales = registrosHoy.stream()
            .mapToInt(RegistroProduccion::getHuevosTotales)
            .sum();
        
        int avesVivas = lote.getCantidadActual() != null ? lote.getCantidadActual() : 0;
        
        double tasaPostura = calcularTasaPostura(totalHuevosTotales, avesVivas);
        
        // Paso 4: Generar alertas según reglas
        
        // REGLA 1: Si Tasa < 70%, ADVERTENCIA por baja producción
        if (tasaPostura < 70.0) {
            Alerta alertaBajaProduccion = new Alerta(
                idLote,
                TipoAlerta.ADVERTENCIA,
                String.format(
                    "Baja producción detectada. Tasa de postura: %.2f%% (Esperado: ≥70%%)",
                    tasaPostura
                )
            );
            repositorioAlerta.guardar(alertaBajaProduccion);
        }
    }
    
    /**
     * Calcula la Tasa de Postura: (Huevos / Aves Vivas) * 100
     * 
     * Métrica técnica que indica la eficiencia de la puesta.
     * - > 85%: Excelente
     * - 70-85%: Normal
     * - < 70%: Preocupante
     * 
     * @param huevosTotales Total de huevos recogidos en el día
     * @param avesVivas Número de gallinas vivas en el lote
     * @return Porcentaje de postura
     */
    private double calcularTasaPostura(int huevosTotales, int avesVivas) {
        if (avesVivas <= 0) {
            return 0.0;
        }
        return (double) huevosTotales / avesVivas * 100.0;
    }
    
    // ===================== TAREA D: REPORTES Y KPIs =====================
    
    /**
     * Calcula el Índice de Conversión Alimenticia (ICA) para un lote.
     * 
     * Fórmula simplificada:
     * ICA = (Alimento Consumido en kg) / (Huevos Producidos en kg)
     * 
     * Asunciones (por falta de registro detallado de alimento):
     * - Consumo promedio: 115g por ave viva por día
     * - Peso promedio de huevo: 60g
     * 
     * Interpretación:
     * - ICA bajo = Mejor eficiencia (menos alimento por huevo)
     * - ICA típico en granjas: 2.0 - 2.5 kg alimento / kg huevos
     * 
     * Ejemplo:
     * - 1000 aves vivas
     * - Consumo: 1000 * 0.115kg = 115 kg de alimento
     * - Producción: 700 huevos * 0.060kg = 42 kg de huevos
     * - ICA = 115 / 42 = 2.74
     * 
     * @param idLote Identificador del lote
     * @return ICA (menor es mejor)
     */
    public double calcularICA(String idLote) {
        if (idLote == null || idLote.isBlank()) {
            throw new IllegalArgumentException("El ID del lote no puede estar vacío");
        }
        
        Optional<Lote> optLote = repositorioLote.buscarPorId(idLote);
        if (optLote.isEmpty()) {
            throw new IllegalArgumentException("Lote no encontrado: " + idLote);
        }
        Lote lote = optLote.get();
        
        // Datos de entrada
        int avesVivas = lote.getCantidadActual() != null ? lote.getCantidadActual() : 0;
        
        // Consumo promedio de alimento (en kg)
        final double CONSUMO_PROMEDIO_KG_POR_AVE = 0.115; // 115g
        double consumoAlimentoKg = avesVivas * CONSUMO_PROMEDIO_KG_POR_AVE;
        
        // Producción del día (suma de huevos totales)
        LocalDate hoy = LocalDate.now();
        List<RegistroProduccion> registrosHoy = 
            repositorioProduccion.buscarPorIdLote(idLote);
        
        // Filtrar solo registros de hoy
        registrosHoy = registrosHoy.stream()
            .filter(r -> r.getFecha().equals(hoy))
            .toList();
        
        int totalHuevos = registrosHoy.stream()
            .mapToInt(RegistroProduccion::getHuevosTotales)
            .sum();
        
        // Peso de huevos producidos (en kg)
        final double PESO_PROMEDIO_HUEVO_KG = 0.060; // 60g
        double pesoHuevosKg = totalHuevos * PESO_PROMEDIO_HUEVO_KG;
        
        // ICA = alimento / huevos
        if (pesoHuevosKg <= 0) {
            return 0.0; // Sin producción
        }
        
        return consumoAlimentoKg / pesoHuevosKg;
    }
    
    /**
     * Genera un reporte semanal resumido para el lote.
     * 
     * Información incluida:
     * - Total de huevos producidos en la semana
     * - Promedio de postura diaria
     * - Cantidad de aves vivas
     * 
     * Este reporte se usa para:
     * - Dashboard del operario
     * - Reportes semanales al director
     * - Análisis de tendencias
     * 
     * @param idLote Identificador del lote
     * @return String con el reporte formateado
     */
    public String generarReporteSemanal(String idLote) {
        if (idLote == null || idLote.isBlank()) {
            throw new IllegalArgumentException("El ID del lote no puede estar vacío");
        }
        
        Optional<Lote> optLote = repositorioLote.buscarPorId(idLote);
        if (optLote.isEmpty()) {
            throw new IllegalArgumentException("Lote no encontrado: " + idLote);
        }
        Lote lote = optLote.get();
        
        // Buscar todos los registros del lote
        List<RegistroProduccion> registrosLote = 
            repositorioProduccion.buscarPorIdLote(idLote);
        
        // Filtrar registros de los últimos 7 días
        LocalDate hoy = LocalDate.now();
        LocalDate hace7Dias = hoy.minusDays(7);
        
        List<RegistroProduccion> registrosSemana = registrosLote.stream()
            .filter(r -> !r.getFecha().isBefore(hace7Dias) && !r.getFecha().isAfter(hoy))
            .toList();
        
        // Calcular métricas
        int totalHuevos = registrosSemana.stream()
            .mapToInt(RegistroProduccion::getHuevosTotales)
            .sum();
        
        int diasConRegistro = (int) registrosSemana.stream()
            .map(RegistroProduccion::getFecha)
            .distinct()
            .count();
        
        double promedioPostura = diasConRegistro > 0 
            ? (double) totalHuevos / diasConRegistro 
            : 0.0;
        
        int avesVivas = lote.getCantidadActual() != null ? lote.getCantidadActual() : 0;
        
        // Generar reporte formateado
        StringBuilder reporte = new StringBuilder();
        reporte.append("╔═══════════════════════════════════════════════════════════╗\n");
        reporte.append("║           REPORTE SEMANAL DE PRODUCCIÓN                   ║\n");
        reporte.append("╠═══════════════════════════════════════════════════════════╣\n");
        reporte.append(String.format("║ Lote: %-47s ║\n", idLote));
        reporte.append(String.format("║ Período: %s a %s         ║\n", hace7Dias, hoy));
        reporte.append("╠═══════════════════════════════════════════════════════════╣\n");
        reporte.append(String.format("║ Total Huevos Semana:        %6d             ║\n", totalHuevos));
        reporte.append(String.format("║ Promedio Diario:            %6.0f huevos       ║\n", promedioPostura));
        reporte.append(String.format("║ Aves Vivas:                 %6d             ║\n", avesVivas));
        reporte.append(String.format("║ Tasa Postura Promedio:      %6.2f%%            ║\n", 
            (avesVivas > 0 && diasConRegistro > 0 ? (double) totalHuevos / avesVivas / diasConRegistro * 100 : 0.0)));
        reporte.append("╚═══════════════════════════════════════════════════════════╝\n");
        
        return reporte.toString();
    }
    
    /**
     * Obtiene todas las alertas críticas pendientes (para el dashboard de emergencias).
     * @return Lista de alertas críticas sin resolver
     */
    public List<Alerta> obtenerAlertasCriticas() {
        return repositorioAlerta.buscarCriticasPendientes();
    }
    
    /**
     * Obtiene un resumen del estado del lote (para el dashboard).
     * @param idLote Identificador del lote
     * @return Número de alertas pendientes
     */
    public long contarAlertasPendientes(String idLote) {
        if (idLote == null || idLote.isBlank()) {
            return 0;
        }
        List<Alerta> alertasPendientes = repositorioAlerta.buscarPendientesPorLote(idLote);
        return alertasPendientes.size();
    }
}
