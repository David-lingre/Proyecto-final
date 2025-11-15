package com.granjapro.infraestructura.persistencia.mongo;

import com.granjapro.dominio.modelos.Alerta;
import com.granjapro.dominio.modelos.Alerta.EstadoAlerta;
import com.granjapro.dominio.modelos.Alerta.TipoAlerta;
import com.granjapro.dominio.repositorios.RepositorioAlerta;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de RepositorioAlerta usando MongoDB.
 * Almacena las alertas en la colección "alertas" de la base de datos granja_db.
 * 
 * Estructura del documento:
 * {
 *   "_id": ObjectId(...),
 *   "fecha": Date,
 *   "idLote": "LOTE-001",
 *   "tipo": "ADVERTENCIA",
 *   "mensaje": "Baja producción detectada",
 *   "estado": "PENDIENTE"
 * }
 */
public class RepositorioAlertaMongo implements RepositorioAlerta {
    
    private final MongoCollection<Document> coleccion;
    
    /**
     * Constructor que recibe la colección de alertas desde la conexión MongoDB.
     * @param coleccion Colección "alertas" de MongoDB
     */
    public RepositorioAlertaMongo(MongoCollection<Document> coleccion) {
        if (coleccion == null) {
            throw new IllegalArgumentException("La colección de alertas no puede ser nula");
        }
        this.coleccion = coleccion;
    }
    
    @Override
    public void guardar(Alerta alerta) {
        if (alerta == null) {
            throw new IllegalArgumentException("La alerta a guardar no puede ser nula");
        }
        
        Document doc = alertaADocument(alerta);
        
        if (alerta.getId() == null) {
            // Nueva alerta: insertar
            alerta.setId(new ObjectId().toString());
            doc.put("_id", new ObjectId(alerta.getId()));
            coleccion.insertOne(doc);
        } else {
            // Alerta existente: actualizar
            coleccion.replaceOne(
                Filters.eq("_id", new ObjectId(alerta.getId())),
                doc
            );
        }
    }
    
    @Override
    public List<Alerta> buscarPendientesPorLote(String idLote) {
        if (idLote == null || idLote.isBlank()) {
            return new ArrayList<>();
        }
        
        return coleccion
            .find(Filters.and(
                Filters.eq("idLote", idLote),
                Filters.eq("estado", "PENDIENTE")
            ))
            .sort(Sorts.descending("fecha"))
            .into(new ArrayList<>())
            .stream()
            .map(this::documentAAlerta)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    }
    
    @Override
    public List<Alerta> buscarPorLote(String idLote) {
        if (idLote == null || idLote.isBlank()) {
            return new ArrayList<>();
        }
        
        return coleccion
            .find(Filters.eq("idLote", idLote))
            .sort(Sorts.descending("fecha"))
            .into(new ArrayList<>())
            .stream()
            .map(this::documentAAlerta)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    }
    
    @Override
    public List<Alerta> buscarPorTipo(TipoAlerta tipo) {
        if (tipo == null) {
            return new ArrayList<>();
        }
        
        return coleccion
            .find(Filters.eq("tipo", tipo.name()))
            .sort(Sorts.descending("fecha"))
            .into(new ArrayList<>())
            .stream()
            .map(this::documentAAlerta)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    }
    
    @Override
    public List<Alerta> buscarPorRangoFechas(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            return new ArrayList<>();
        }
        
        Date desdeDate = convertirADate(desde);
        Date hastaDate = convertirADate(hasta.plusDays(1)); // Hasta el final del día
        
        return coleccion
            .find(Filters.and(
                Filters.gte("fecha", desdeDate),
                Filters.lt("fecha", hastaDate)
            ))
            .sort(Sorts.descending("fecha"))
            .into(new ArrayList<>())
            .stream()
            .map(this::documentAAlerta)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    }
    
    @Override
    public List<Alerta> buscarCriticasPendientes() {
        return coleccion
            .find(Filters.and(
                Filters.eq("tipo", "CRITICA"),
                Filters.eq("estado", "PENDIENTE")
            ))
            .sort(Sorts.descending("fecha"))
            .into(new ArrayList<>())
            .stream()
            .map(this::documentAAlerta)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .toList();
    }
    
    @Override
    public void resolver(String idAlerta) {
        if (idAlerta == null || idAlerta.isBlank()) {
            throw new IllegalArgumentException("El ID de la alerta no puede estar vacío");
        }
        
        Document updateDoc = new Document();
        updateDoc.put("$set", new Document("estado", "RESUELTA"));
        
        coleccion.updateOne(
            Filters.eq("_id", new ObjectId(idAlerta)),
            updateDoc
        );
    }
    
    @Override
    public long contarPendientes() {
        return coleccion.countDocuments(Filters.eq("estado", "PENDIENTE"));
    }
    
    @Override
    public long contarPorTipo(TipoAlerta tipo) {
        if (tipo == null) {
            return 0;
        }
        return coleccion.countDocuments(Filters.eq("tipo", tipo.name()));
    }
    
    // ===================== CONVERSIONES DOCUMENTO <-> ALERTA =====================
    
    /**
     * Convierte un documento MongoDB a una entidad Alerta.
     */
    private Optional<Alerta> documentAAlerta(Document doc) {
        try {
            String id = doc.getObjectId("_id").toString();
            LocalDate fecha = convertirALocalDate(doc.getDate("fecha"));
            String idLote = doc.getString("idLote");
            TipoAlerta tipo = TipoAlerta.valueOf(doc.getString("tipo"));
            String mensaje = doc.getString("mensaje");
            EstadoAlerta estado = EstadoAlerta.valueOf(doc.getString("estado"));
            
            return Optional.of(new Alerta(id, fecha, idLote, tipo, mensaje, estado));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
    
    /**
     * Convierte una entidad Alerta a un documento MongoDB.
     */
    private Document alertaADocument(Alerta alerta) {
        Document doc = new Document();
        
        if (alerta.getId() != null) {
            doc.put("_id", new ObjectId(alerta.getId()));
        }
        
        doc.put("fecha", convertirADate(alerta.getFecha()));
        doc.put("idLote", alerta.getIdLote());
        doc.put("tipo", alerta.getTipo().name());
        doc.put("mensaje", alerta.getMensaje());
        doc.put("estado", alerta.getEstado().name());
        
        return doc;
    }
    
    /**
     * Convierte LocalDate a Date (para MongoDB).
     */
    private Date convertirADate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * Convierte Date a LocalDate (desde MongoDB).
     */
    private LocalDate convertirALocalDate(Date date) {
        if (date == null) {
            return LocalDate.now();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }
}
