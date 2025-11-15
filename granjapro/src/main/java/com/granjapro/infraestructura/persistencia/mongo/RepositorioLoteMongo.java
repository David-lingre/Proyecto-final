package com.granjapro.infraestructura.persistencia.mongo;

import com.granjapro.dominio.modelos.Lote;
import com.granjapro.dominio.repositorios.RepositorioLote;
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
 * Implementación del repositorio de Lote usando MongoDB.
 * Transforma entre objetos de dominio (Lote) y documentos de MongoDB.
 */
public class RepositorioLoteMongo implements RepositorioLote {
    
    private static final String NOMBRE_COLECCION = "lotes";
    private MongoCollection<Document> coleccion;
    
    /**
     * Constructor que inicializa la colección de MongoDB.
     */
    public RepositorioLoteMongo() {
        MongoDatabase baseDatos = ConexionMongo.obtenerInstancia().obtenerBaseDatos();
        this.coleccion = baseDatos.getCollection(NOMBRE_COLECCION);
    }
    
    /**
     * Guarda un nuevo lote o actualiza uno existente.
     *
     * @param lote el lote a guardar
     * @return el lote guardado (con ID asignado si es nuevo)
     */
    @Override
    public Lote guardar(Lote lote) {
        Document documento = convertirLoteADocumento(lote);
        
        if (lote.getId() == null) {
            // Es un lote nuevo
            coleccion.insertOne(documento);
            lote.setId(documento.getObjectId("_id").toString());
        } else {
            // Es una actualización
            coleccion.replaceOne(
                Filters.eq("_id", new ObjectId(lote.getId())),
                documento
            );
        }
        
        return lote;
    }
    
    /**
     * Busca un lote por su identificador único.
     *
     * @param id el identificador del lote
     * @return un Optional contiene el lote si existe, vacío si no
     */
    @Override
    public Optional<Lote> buscarPorId(String id) {
        try {
            Document documento = coleccion.find(
                Filters.eq("_id", new ObjectId(id))
            ).first();
            
            if (documento != null) {
                return Optional.of(convertirDocumentoALote(documento));
            }
        } catch (IllegalArgumentException e) {
            // ID inválido
            return Optional.empty();
        }
        
        return Optional.empty();
    }
    
    /**
     * Obtiene todos los lotes registrados en el sistema.
     *
     * @return una lista con todos los lotes
     */
    @Override
    public List<Lote> buscarTodos() {
        List<Lote> lotes = new ArrayList<>();
        
        for (Document documento : coleccion.find()) {
            lotes.add(convertirDocumentoALote(documento));
        }
        
        return lotes;
    }
    
    /**
     * Convierte un objeto de dominio Lote a un Documento de MongoDB.
     *
     * @param lote el lote a convertir
     * @return el documento MongoDB correspondiente
     */
    private Document convertirLoteADocumento(Lote lote) {
        Document documento = new Document()
            .append("codigo", lote.getCodigo())
            .append("raza", lote.getRaza())
            .append("cantidadInicial", lote.getCantidadInicial())
            .append("cantidadActual", lote.getCantidadActual())
            .append("fechaIngreso", lote.getFechaIngreso().toString())
            .append("idCorral", lote.getIdCorral());
        
        if (lote.getId() != null) {
            documento.append("_id", new ObjectId(lote.getId()));
        }
        
        return documento;
    }
    
    /**
     * Convierte un Documento de MongoDB a un objeto de dominio Lote.
     *
     * @param documento el documento a convertir
     * @return el lote correspondiente
     */
    private Lote convertirDocumentoALote(Document documento) {
        return new Lote(
            documento.getObjectId("_id").toString(),
            documento.getString("codigo"),
            documento.getString("raza"),
            documento.getInteger("cantidadInicial"),
            documento.getInteger("cantidadActual"),
            LocalDate.parse(documento.getString("fechaIngreso")),
            documento.getString("idCorral")
        );
    }
}
