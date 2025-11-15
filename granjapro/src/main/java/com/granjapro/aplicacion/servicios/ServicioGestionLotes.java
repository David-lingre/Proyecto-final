package com.granjapro.aplicacion.servicios;

import com.granjapro.dominio.modelos.Lote;
import com.granjapro.dominio.repositorios.RepositorioLote;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de Gestión de Lotes.
 * 
 * Implementa la lógica de negocio relacionada con la creación y mantenimiento de lotes.
 * Utiliza inyección de dependencias por constructor para interactuar con el repositorio.
 */
public class ServicioGestionLotes {
    
    private RepositorioLote repositorioLote;
    
    /**
     * Constructor que recibe el repositorio por inyección de dependencias.
     *
     * @param repositorioLote la implementación del repositorio de lotes
     * @throws IllegalArgumentException si repositorioLote es nulo
     */
    public ServicioGestionLotes(RepositorioLote repositorioLote) {
        if (repositorioLote == null) {
            throw new IllegalArgumentException("El repositorio no puede ser nulo");
        }
        this.repositorioLote = repositorioLote;
    }
    
    /**
     * Crea un nuevo lote en el sistema.
     * 
     * Crea un objeto Lote con los parámetros proporcionados,
     * asigna la fecha de ingreso como hoy, y lo guarda en el repositorio.
     *
     * @param codigo el código único del lote
     * @param raza la raza de las gallinas (ej: "Rhode Island Red")
     * @param cantidadInicial la cantidad inicial de gallinas
     * @param idCorral el identificador del corral donde se alojará el lote
     * @return el lote creado y guardado (con ID asignado)
     * 
     * @throws IllegalArgumentException si algún parámetro es nulo o inválido,
     *         o si la cantidad inicial es negativa o cero
     */
    public Lote crearLote(String codigo, String raza, Integer cantidadInicial, String idCorral) {
        // Validaciones de entrada
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El código del lote no puede estar vacío");
        }
        if (raza == null || raza.trim().isEmpty()) {
            throw new IllegalArgumentException("La raza no puede estar vacía");
        }
        if (cantidadInicial == null || cantidadInicial <= 0) {
            throw new IllegalArgumentException("La cantidad inicial debe ser mayor a cero");
        }
        if (idCorral == null || idCorral.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del corral no puede estar vacío");
        }
        
        // Crear el lote con la fecha de ingreso como hoy
        Lote lote = new Lote(
            null, // ID será asignado por la BD
            codigo,
            raza,
            cantidadInicial,
            cantidadInicial, // cantidad actual = cantidad inicial
            LocalDate.now(),
            idCorral
        );
        
        // Guardar en el repositorio y retornar
        return repositorioLote.guardar(lote);
    }
    
    /**
     * Registra la mortalidad en un lote.
     * 
     * Busca el lote por ID, reduce la cantidad actual según el número de muertes,
     * y guarda los cambios. La validación de que la cantidad no sea negativa
     * y no exceda la cantidad inicial se realiza en el setter del lote.
     *
     * @param idLote el identificador del lote
     * @param cantidad la cantidad de gallinas muertas
     * 
     * @throws IllegalArgumentException si la cantidad es negativa o cero,
     *         o si la cantidad resultante violaría las validaciones del lote
     * @throws RuntimeException si el lote no existe en el repositorio
     */
    public void registrarMortalidad(String idLote, int cantidad) {
        if (cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad de muertes debe ser mayor a cero");
        }
        
        // Buscar el lote
        Optional<Lote> loteOpt = repositorioLote.buscarPorId(idLote);
        if (!loteOpt.isPresent()) {
            throw new RuntimeException("No existe un lote con el ID: " + idLote);
        }
        
        Lote lote = loteOpt.get();
        
        // Calcular nueva cantidad y llamar al setter (que dispara validaciones)
        int nuevaCantidad = lote.getCantidadActual() - cantidad;
        lote.setCantidadActual(nuevaCantidad);
        
        // Guardar los cambios
        repositorioLote.guardar(lote);
    }
    
    /**
     * Obtiene un lote específico por su identificador.
     *
     * @param idLote el identificador del lote
     * @return el lote encontrado
     * 
     * @throws RuntimeException si el lote no existe
     */
    public Lote obtenerLote(String idLote) {
        Optional<Lote> loteOpt = repositorioLote.buscarPorId(idLote);
        
        if (!loteOpt.isPresent()) {
            throw new RuntimeException("No existe un lote con el ID: " + idLote);
        }
        
        return loteOpt.get();
    }
    
    /**
     * Lista todos los lotes registrados en el sistema.
     *
     * @return una lista con todos los lotes, puede estar vacía si no hay lotes
     */
    public List<Lote> listarLotes() {
        return repositorioLote.buscarTodos();
    }
}
