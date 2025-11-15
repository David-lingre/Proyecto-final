package com.granjapro.infraestructura.persistencia.mongo;

import com.granjapro.dominio.modelos.Usuario;
import com.granjapro.dominio.modelos.Rol;
import com.granjapro.dominio.repositorios.RepositorioUsuario;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de RepositorioUsuario usando MongoDB.
 * 
 * Responsabilidades:
 * - Mapear objetos Usuario ↔ Documentos MongoDB
 * - Ejecutar operaciones CRUD en MongoDB
 * - Manejar la persistencia de usuarios
 * 
 * @author David
 * @version 1.0
 */
public class RepositorioUsuarioMongo implements RepositorioUsuario {
    
    private static final String COLECCION_USUARIOS = "usuarios";
    private MongoCollection<Document> coleccion;
    
    /**
     * Constructor que recibe la colección MongoDB.
     * 
     * Inyección de dependencia: ConexionMongo proporciona la colección.
     * 
     * @param coleccion colección MongoDB de usuarios
     */
    public RepositorioUsuarioMongo(MongoCollection<Document> coleccion) {
        if (coleccion == null) {
            throw new IllegalArgumentException("Colección no puede ser nula");
        }
        this.coleccion = coleccion;
    }
    
    /**
     * Busca un usuario por nombre (único en el sistema).
     * 
     * Conversión:
     *   MongoDB Document → Objeto Usuario
     * 
     * @param nombre nombre a buscar
     * @return Usuario encontrado o null
     */
    @Override
    public Usuario buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        
        // Buscar documento con nombre igual
        Document doc = coleccion.find(Filters.eq("nombre", nombre)).first();
        
        if (doc == null) {
            return null;
        }
        
        // Convertir Document a Usuario
        return documentAUsuario(doc);
    }
    
    /**
     * Busca un usuario por su ID (ObjectId de MongoDB).
     * 
     * @param id ID del usuario
     * @return Usuario encontrado o null
     */
    @Override
    public Usuario buscarPorId(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        
        try {
            ObjectId objectId = new ObjectId(id);
            Document doc = coleccion.find(Filters.eq("_id", objectId)).first();
            
            if (doc == null) {
                return null;
            }
            
            return documentAUsuario(doc);
        } catch (IllegalArgumentException e) {
            return null;  // ID inválido
        }
    }
    
    /**
     * Guarda un nuevo usuario en MongoDB.
     * MongoDB genera automáticamente el _id.
     * 
     * @param usuario usuario a guardar
     */
    @Override
    public void guardar(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Usuario no puede ser nulo");
        }
        
        // Verificar que no existe
        if (existePorNombre(usuario.getNombre())) {
            throw new RuntimeException("Usuario '" + usuario.getNombre() + "' ya existe");
        }
        
        // Convertir Usuario a Document
        Document doc = usuarioADocument(usuario);
        
        // Insertar en MongoDB
        coleccion.insertOne(doc);
    }
    
    /**
     * Actualiza un usuario existente.
     * 
     * @param usuario usuario con cambios (debe tener ID)
     */
    @Override
    public void actualizar(Usuario usuario) {
        if (usuario == null || usuario.getId() == null) {
            throw new IllegalArgumentException("Usuario debe tener ID para actualizar");
        }
        
        try {
            ObjectId objectId = new ObjectId(usuario.getId());
            Document doc = usuarioADocument(usuario);
            
            coleccion.replaceOne(Filters.eq("_id", objectId), doc);
        } catch (Exception e) {
            throw new RuntimeException("Error al actualizar usuario", e);
        }
    }
    
    /**
     * Elimina un usuario.
     * 
     * @param id ID del usuario a eliminar
     */
    @Override
    public void eliminar(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("ID no puede estar vacío");
        }
        
        try {
            ObjectId objectId = new ObjectId(id);
            coleccion.deleteOne(Filters.eq("_id", objectId));
        } catch (Exception e) {
            throw new RuntimeException("Error al eliminar usuario", e);
        }
    }
    
    /**
     * Obtiene todos los usuarios.
     * 
     * @return lista de usuarios
     */
    @Override
    public List<Usuario> obtenerTodos() {
        List<Usuario> usuarios = new ArrayList<>();
        
        for (Document doc : coleccion.find()) {
            usuarios.add(documentAUsuario(doc));
        }
        
        return usuarios;
    }
    
    /**
     * Comprueba si existe usuario con ese nombre.
     * 
     * @param nombre nombre a verificar
     * @return true si existe
     */
    @Override
    public boolean existePorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return false;
        }
        
        Document doc = coleccion.find(Filters.eq("nombre", nombre)).first();
        return doc != null;
    }
    
    // ═══════════════════════════════════════════════════════════
    // MAPEO: Document ↔ Usuario
    // ═══════════════════════════════════════════════════════════
    
    /**
     * Convierte un Documento MongoDB a objeto Usuario.
     * 
     * MongoDB guarda:
     * {
     *   "_id": ObjectId,
     *   "nombre": "juan",
     *   "passwordHasheado": "9c9064c59f1ffa...",
     *   "rol": "OPERARIO",
     *   "activo": true
     * }
     * 
     * @param doc documento MongoDB
     * @return Usuario con los datos del documento
     */
    private Usuario documentAUsuario(Document doc) {
        Usuario usuario = new Usuario();
        
        // Extraer campos del Document
        usuario.setId(doc.getObjectId("_id").toString());
        usuario.setNombre(doc.getString("nombre"));
        usuario.setRol(Rol.valueOf(doc.getString("rol")));
        usuario.setActivo(doc.getBoolean("activo"));
        
        // Nota: passwordHasheado se debe acceder directamente
        // porque no hay setter que hashee nuevamente
        try {
            java.lang.reflect.Field field = Usuario.class.getDeclaredField("passwordHasheado");
            field.setAccessible(true);
            field.set(usuario, doc.getString("passwordHasheado"));
        } catch (Exception e) {
            throw new RuntimeException("Error al mapear password", e);
        }
        
        return usuario;
    }
    
    /**
     * Convierte un objeto Usuario a Documento MongoDB.
     * 
     * @param usuario usuario a convertir
     * @return documento MongoDB
     */
    private Document usuarioADocument(Usuario usuario) {
        Document doc = new Document();
        
        // Si tiene ID, usarlo; si no, MongoDB lo genera
        if (usuario.getId() != null) {
            doc.append("_id", new ObjectId(usuario.getId()));
        }
        
        doc.append("nombre", usuario.getNombre());
        doc.append("passwordHasheado", usuario.getPasswordHasheado());
        doc.append("rol", usuario.getRol().toString());
        doc.append("activo", usuario.isActivo());
        
        return doc;
    }
}
