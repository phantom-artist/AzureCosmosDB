package com.phantomartist.azure.cosmosdb;

/**
 * Title: Connection
 *
 * Description: Represents a cosmos DB connection 
 */
public interface Connection {

    /**
     * Get the collection link for this connection
     * 
     * @return String the collection link
     */
    String getCollectionLink();
    
    /**
     * Generate a Query from a query string
     * 
     * @param query the query
     * 
     * @return Query that can be run asynchronously or in blocking mode
     */
    Query generateQuery( final String query );
    
    /**
     * Generate a Statement to perform a CREATE/UPDATE/DELETE operation.
     * (Reads are handled by {@link #generateQuery(String)})
     * 
     * @return Statement the executable statement that can be run asynchronously or in blocking mode
     */
    Statement generateStatement();
}
