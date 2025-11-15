package com.granjapro.infraestructura.persistencia.mongo;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

/**
 * Singleton que gestiona la conexión a MongoDB.
 * Proporciona una instancia única de MongoDatabase para toda la aplicación.
 */
public class ConexionMongo {
    
    private static ConexionMongo instancia;
    private MongoClient cliente;
    private MongoDatabase baseDatos;
    
    /**
     * URI de conexión a MongoDB.
     * Modificar según la configuración del servidor MongoDB.
     */
    private static final String URI_CONEXION = "mongodb://localhost:27017";
    
    /**
     * Nombre de la base de datos
     */
    private static final String NOMBRE_BD = "granjapro";
    
    /**
     * Constructor privado para implementar el patrón Singleton
     */
    private ConexionMongo() {
        conectar();
    }
    
    /**
     * Obtiene la instancia única de ConexionMongo.
     *
     * @return la instancia singleton de ConexionMongo
     */
    public static synchronized ConexionMongo obtenerInstancia() {
        if (instancia == null) {
            instancia = new ConexionMongo();
        }
        return instancia;
    }
    
    /**
     * Realiza la conexión a MongoDB.
     * Se ejecuta automáticamente en el constructor.
     */
    private void conectar() {
        try {
            cliente = MongoClients.create(URI_CONEXION);
            baseDatos = cliente.getDatabase(NOMBRE_BD);
            System.out.println("Conexión exitosa a MongoDB: " + NOMBRE_BD);
        } catch (Exception e) {
            System.err.println("Error al conectarse a MongoDB: " + e.getMessage());
            throw new RuntimeException("No se pudo establecer conexión con MongoDB", e);
        }
    }
    
    /**
     * Obtiene la instancia de MongoDatabase.
     *
     * @return la base de datos MongoDB
     */
    public MongoDatabase obtenerBaseDatos() {
        return baseDatos;
    }
    
    /**
     * Cierra la conexión con MongoDB.
     * Debe llamarse al cerrar la aplicación.
     */
    public void cerrar() {
        if (cliente != null) {
            cliente.close();
            System.out.println("Conexión a MongoDB cerrada");
        }
    }
}
