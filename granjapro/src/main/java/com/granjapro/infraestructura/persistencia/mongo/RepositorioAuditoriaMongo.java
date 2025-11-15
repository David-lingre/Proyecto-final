package com.granjapro.infraestructura.persistencia.mongo;

import com.granjapro.dominio.modelos.LogAuditoria;
import com.granjapro.dominio.repositorios.RepositorioAuditoria;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Implementación de RepositorioAuditoria usando MongoDB.
 * 
 * Almacena LogAuditoria en colección "auditoria" con índices para búsquedas rápidas:
 * - Índice en idUsuario (buscar cambios por usuario)
 * - Índice en idEntidadAfectada (buscar cambios a una entidad)
 * - Índice en fecha (buscar por rango temporal)
 * 
 * @author Sistema
 * @version 1.0
 */
public class RepositorioAuditoriaMongo implements RepositorioAuditoria {
    
    private final MongoCollection<Document> coleccion;
    
    /**
     * Constructor que recibe la colección MongoDB.
     * 
     * @param coleccion colección "auditoria" en MongoDB
     */
    public RepositorioAuditoriaMongo(MongoCollection<Document> coleccion) {
        if (coleccion == null) {
            throw new IllegalArgumentException("Colección no puede ser null");
        }
        this.coleccion = coleccion;
    }
    
    // ═══════════════════════════════════════════════════════════
    // MÉTODOS CRUD
    // ═══════════════════════════════════════════════════════════
    
    @Override
    public void guardar(LogAuditoria log) {
        if (log == null) {
            throw new IllegalArgumentException("LogAuditoria no puede ser null");
        }
        
        Document doc = auditariaADocument(log);
        coleccion.insertOne(doc);
    }
    
    // ═══════════════════════════════════════════════════════════
    // BÚSQUEDAS
    // ═══════════════════════════════════════════════════════════
    
    @Override
    public List<LogAuditoria> buscarPorUsuario(String idUsuario) {
        if (idUsuario == null || idUsuario.isBlank()) {
            return new ArrayList<>();
        }
        
        List<LogAuditoria> resultados = new ArrayList<>();
        coleccion
                .find(Filters.eq("idUsuario", idUsuario))
                .sort(Sorts.descending("fecha"))  // Más recientes primero
                .forEach(doc -> resultados.add(documentAAuditoria(doc)));
        
        return resultados;
    }
    
    @Override
    public List<LogAuditoria> buscarPorEntidad(String idEntidad) {
        if (idEntidad == null || idEntidad.isBlank()) {
            return new ArrayList<>();
        }
        
        List<LogAuditoria> resultados = new ArrayList<>();
        coleccion
                .find(Filters.eq("idEntidadAfectada", idEntidad))
                .sort(Sorts.ascending("fecha"))  // Cronológico
                .forEach(doc -> resultados.add(documentAAuditoria(doc)));
        
        return resultados;
    }
    
    @Override
    public List<LogAuditoria> buscarPorRangoFechas(LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null || hasta == null) {
            return new ArrayList<>();
        }
        
        Date desdeDate = Date.from(desde.atZone(ZoneId.systemDefault()).toInstant());
        Date hastaDate = Date.from(hasta.atZone(ZoneId.systemDefault()).toInstant());
        
        List<LogAuditoria> resultados = new ArrayList<>();
        coleccion
                .find(Filters.and(
                        Filters.gte("fecha", desdeDate),
                        Filters.lte("fecha", hastaDate)
                ))
                .sort(Sorts.descending("fecha"))
                .forEach(doc -> resultados.add(documentAAuditoria(doc)));
        
        return resultados;
    }
    
    @Override
    public List<LogAuditoria> buscarPorTipo(String tipoEntidad) {
        if (tipoEntidad == null || tipoEntidad.isBlank()) {
            return new ArrayList<>();
        }
        
        List<LogAuditoria> resultados = new ArrayList<>();
        coleccion
                .find(Filters.eq("tipoEntidad", tipoEntidad))
                .sort(Sorts.descending("fecha"))
                .forEach(doc -> resultados.add(documentAAuditoria(doc)));
        
        return resultados;
    }
    
    @Override
    public List<LogAuditoria> obtenerTodos() {
        List<LogAuditoria> resultados = new ArrayList<>();
        coleccion
                .find()
                .sort(Sorts.descending("fecha"))
                .forEach(doc -> resultados.add(documentAAuditoria(doc)));
        
        return resultados;
    }
    
    @Override
    public long contar(String idEntidad) {
        if (idEntidad == null || idEntidad.isBlank()) {
            return 0;
        }
        return coleccion.countDocuments(Filters.eq("idEntidadAfectada", idEntidad));
    }
    
    // ═══════════════════════════════════════════════════════════
    // CONVERSIONES DOCUMENT ↔ OBJETO
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Convierte un Document de MongoDB a un objeto LogAuditoria.
     */
    private LogAuditoria documentAAuditoria(Document doc) {
        LogAuditoria auditoria = new LogAuditoria();
        
        if (doc.containsKey("_id")) {
            auditoria.setId(doc.getObjectId("_id").toString());
        }
        
        if (doc.containsKey("fecha")) {
            Date fecha = doc.getDate("fecha");
            auditoria.setFecha(
                fecha.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
            );
        }
        
        if (doc.containsKey("idUsuario")) {
            auditoria.setIdUsuario(doc.getString("idUsuario"));
        }
        
        if (doc.containsKey("nombreUsuario")) {
            auditoria.setNombreUsuario(doc.getString("nombreUsuario"));
        }
        
        if (doc.containsKey("idEntidadAfectada")) {
            auditoria.setIdEntidadAfectada(doc.getString("idEntidadAfectada"));
        }
        
        if (doc.containsKey("tipoEntidad")) {
            auditoria.setTipoEntidad(doc.getString("tipoEntidad"));
        }
        
        if (doc.containsKey("campoModificado")) {
            auditoria.setCampoModificado(doc.getString("campoModificado"));
        }
        
        if (doc.containsKey("valorAnterior")) {
            auditoria.setValorAnterior(doc.getString("valorAnterior"));
        }
        
        if (doc.containsKey("valorNuevo")) {
            auditoria.setValorNuevo(doc.getString("valorNuevo"));
        }
        
        if (doc.containsKey("motivo")) {
            auditoria.setMotivo(doc.getString("motivo"));
        }
        
        if (doc.containsKey("accion")) {
            auditoria.setAccion(doc.getString("accion"));
        }
        
        return auditoria;
    }
    
    /**
     * Convierte un objeto LogAuditoria a un Document de MongoDB.
     */
    private Document auditariaADocument(LogAuditoria auditoria) {
        Document doc = new Document();
        
        if (auditoria.getId() != null && !auditoria.getId().isBlank()) {
            doc.append("_id", new ObjectId(auditoria.getId()));
        }
        
        if (auditoria.getFecha() != null) {
            Date fecha = Date.from(
                auditoria.getFecha()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
            );
            doc.append("fecha", fecha);
        }
        
        doc.append("idUsuario", auditoria.getIdUsuario());
        doc.append("nombreUsuario", auditoria.getNombreUsuario());
        doc.append("idEntidadAfectada", auditoria.getIdEntidadAfectada());
        doc.append("tipoEntidad", auditoria.getTipoEntidad());
        doc.append("campoModificado", auditoria.getCampoModificado());
        doc.append("valorAnterior", auditoria.getValorAnterior());
        doc.append("valorNuevo", auditoria.getValorNuevo());
        doc.append("motivo", auditoria.getMotivo());
        doc.append("accion", auditoria.getAccion());
        
        return doc;
    }
}
