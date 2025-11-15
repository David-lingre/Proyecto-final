package com.granjapro.infraestructura.persistencia.mongo;

import com.granjapro.dominio.modelos.RegistroProduccion;
import com.granjapro.dominio.repositorios.RepositorioRegistroProduccion;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del repositorio de RegistroProduccion usando MongoDB.
 * Transforma entre objetos de dominio (RegistroProduccion) y documentos de MongoDB.
 */
public class RepositorioRegistroProduccionMongo implements RepositorioRegistroProduccion {
    
    private static final String NOMBRE_COLECCION = "registrosProduccion";
    private MongoCollection<Document> coleccion;
    
    /**
     * Constructor que inicializa la colección de MongoDB.
     */
    public RepositorioRegistroProduccionMongo() {
        MongoDatabase baseDatos = ConexionMongo.obtenerInstancia().obtenerBaseDatos();
        this.coleccion = baseDatos.getCollection(NOMBRE_COLECCION);
    }
    
    /**
     * Guarda un nuevo registro o actualiza uno existente.
     *
     * @param registroProduccion el registro a guardar
     * @return el registro guardado (con ID asignado si es nuevo)
     */
    @Override
    public RegistroProduccion guardar(RegistroProduccion registroProduccion) {
        Document documento = convertirRegistroADocumento(registroProduccion);
        
        if (registroProduccion.getId() == null) {
            // Es un registro nuevo
            coleccion.insertOne(documento);
            registroProduccion.setId(documento.getObjectId("_id").toString());
        } else {
            // Es una actualización
            coleccion.replaceOne(
                Filters.eq("_id", new ObjectId(registroProduccion.getId())),
                documento
            );
        }
        
        return registroProduccion;
    }
    
    /**
     * Busca un registro por su identificador único.
     *
     * @param id el identificador del registro
     * @return un Optional contiene el registro si existe, vacío si no
     */
    @Override
    public Optional<RegistroProduccion> buscarPorId(String id) {
        try {
            Document documento = coleccion.find(
                Filters.eq("_id", new ObjectId(id))
            ).first();
            
            if (documento != null) {
                return Optional.of(convertirDocumentoARegistro(documento));
            }
        } catch (IllegalArgumentException e) {
            // ID inválido
            return Optional.empty();
        }
        
        return Optional.empty();
    }
    
    /**
     * Obtiene todos los registros registrados en el sistema.
     *
     * @return una lista con todos los registros
     */
    @Override
    public List<RegistroProduccion> buscarTodos() {
        List<RegistroProduccion> registros = new ArrayList<>();
        
        for (Document documento : coleccion.find()) {
            registros.add(convertirDocumentoARegistro(documento));
        }
        
        return registros;
    }
    
    /**
     * Busca todos los registros de un lote específico.
     *
     * @param idLote el identificador del lote
     * @return una lista con los registros del lote
     */
    @Override
    public List<RegistroProduccion> buscarPorIdLote(String idLote) {
        List<RegistroProduccion> registros = new ArrayList<>();
        
        for (Document documento : coleccion.find(Filters.eq("idLote", idLote))) {
            registros.add(convertirDocumentoARegistro(documento));
        }
        
        return registros;
    }
    
    /**
     * Convierte un objeto de dominio RegistroProduccion a un Documento de MongoDB.
     *
     * @param registroProduccion el registro a convertir
     * @return el documento MongoDB correspondiente
     */
    private Document convertirRegistroADocumento(RegistroProduccion registroProduccion) {
        Document documento = new Document()
            .append("idLote", registroProduccion.getIdLote())
            .append("fecha", registroProduccion.getFecha().toString())
            .append("huevosTotales", registroProduccion.getHuevosTotales())
            .append("huevosRotos", registroProduccion.getHuevosRotos());
        
        if (registroProduccion.getId() != null) {
            documento.append("_id", new ObjectId(registroProduccion.getId()));
        }
        
        return documento;
    }
    
    /**
     * Convierte un Documento de MongoDB a un objeto de dominio RegistroProduccion.
     *
     * @param documento el documento a convertir
     * @return el registro correspondiente
     */
    private RegistroProduccion convertirDocumentoARegistro(Document documento) {
        return new RegistroProduccion(
            documento.getObjectId("_id").toString(),
            documento.getString("idLote"),
            LocalDate.parse(documento.getString("fecha")),
            documento.getInteger("huevosTotales"),
            documento.getInteger("huevosRotos")
        );
    }
}
