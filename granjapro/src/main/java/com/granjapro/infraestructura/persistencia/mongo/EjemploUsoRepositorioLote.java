package com.granjapro.infraestructura.persistencia.mongo;

import com.granjapro.dominio.modelos.Lote;
import com.granjapro.dominio.repositorios.RepositorioLote;
import java.time.LocalDate;

/**
 * Ejemplo de uso de RepositorioLoteMongo.
 * Este archivo es solo referencial y no es parte de la compilación principal.
 * Borrar o mover a tests cuando esté listo para pruebas unitarias.
 */
public class EjemploUsoRepositorioLote {
    
    /**
     * Ejemplo de cómo usar el repositorio de Lotes.
     */
    public static void main(String[] args) {
        // Obtener la instancia del repositorio
        RepositorioLote repositorio = new RepositorioLoteMongo();
        
        // Crear un nuevo lote
        Lote lote = new Lote();
        lote.setCodigo("LOTE-001");
        lote.setRaza("Leghorn Blanca");
        lote.setCantidadInicial(100);
        lote.setCantidadActual(100);
        lote.setFechaIngreso(LocalDate.now());
        lote.setIdCorral("CORRAL-01");
        
        // Guardar el lote
        Lote lotGuardado = repositorio.guardar(lote);
        System.out.println("Lote guardado con ID: " + lotGuardado.getId());
        
        // Buscar el lote por ID
        var loteEncontrado = repositorio.buscarPorId(lotGuardado.getId());
        if (loteEncontrado.isPresent()) {
            System.out.println("Lote encontrado: " + loteEncontrado.get().getCodigo());
        }
        
        // Obtener todos los lotes
        var todoLosLotes = repositorio.buscarTodos();
        System.out.println("Total de lotes: " + todoLosLotes.size());
        
        // Actualizar el lote
        lotGuardado.setCantidadActual(95);
        repositorio.guardar(lotGuardado);
        System.out.println("Lote actualizado");
    }
}
