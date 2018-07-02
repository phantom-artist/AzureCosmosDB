package com.phantomartist.azure.cosmosdb;

/**
 * Title: CosmosDBClient
 *
 * Description: Typically only 1 client is required per-JVM as connection management is shared and threadsafe,
 * so this class will likely only be created once in a 'startup' method and the resulting object shared around
 * the application. If more than 1 database must be accessed, another client should be created for that database.
 */
public interface DBClient extends AutoCloseable {

    /**
     * Get the CosmosDB Endpoint that this client is pointed at.
     * 
     * @return String the endpoint
     */
    String getEndPoint();
    
    /**
     * Get the Master Key that this client uses to connect with the Endpoint.
     * 
     * @return String the master key
     */
    String getMasterKey();
    
    /**
     * Get a connection to a specific database service and collection.
     * 
     * @param db the database to get a connection for
     * @param collection the collection to operate on
     * 
     * @return CosmosDBConnection the connection
     */
    Connection getConnection( final String db, final String collection );
    
    /**
     * Close the client and release resources.
     */
    void close();
}
