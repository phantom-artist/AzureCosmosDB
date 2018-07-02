package com.phantomartist.azure.cosmosdb.impl;

import java.util.ArrayList;
import java.util.List;

import com.phantomartist.azure.cosmosdb.Connection;
import com.phantomartist.azure.cosmosdb.DBClient;
import com.phantomartist.azure.cosmosdb.Document;

/**
 * Title: CosmosDBDAO
 *
 * Description: Example of creating a traditional "blocking" DAO-style class using the DBClient API.
 * 
 * Assumes a CosmosDB "mydb" with a collection "product"
 */
public class CosmosDBDAO {

	private static final String SERVICE_ENDPOINT_ADDRESS = "<cosmosdb service endpoint address>"; // Replace with your value
	private static final String ACCESS_KEY = "<cosmosdb connection key>"; // Replace with your value
	
	private static final String DB = "mydb";
    private static final String COLLECTION = "product";
    
    private DBClient client;
    private Connection conn;
    
    public CosmosDBDAO() {
        
        // This would be a singleton in the JVM and could be injected into each DAO that needs the client
        client = DBClientBuilderImpl.getDefault().getDefaultCosmosDBAsyncClient( 
            SERVICE_ENDPOINT_ADDRESS,
            ACCESS_KEY
        );

        // This should be generated per-DAO/collection
        conn = client.getConnection( DB, COLLECTION );
    }
    
    /**
     * Find document by PK
     * 
     * @param pk the primary key
     * 
     * @return Document the document
     */
    public Document find( String pk ) {
        
        final List<Document> results = new ArrayList<>();
        
        // Implement blocking behaviour in query if you want to wait for execute() 
        // to complete before returning to caller
        conn.generateQuery( "SELECT * FROM product WHERE product.id = @id" )
            .addParam( "@id", pk )
            .setBlocking( true )
            .execute(
                onPage -> {
                    results.addAll( onPage );
            });
        
        return results.isEmpty() ? null : results.get( 0 );
    }
    
    /**
     * Find all documents in Product collection
     * 
     * @return List of Document
     */
    public List<Document> findAll() {
        
        final List<Document> results = new ArrayList<>();
        
        conn.generateQuery( "SELECT * FROM Product" )
            .setBlocking( true )
            .execute(
                onPage -> {
                    results.addAll( onPage );
            });
        
        return results;
    }
    
    /**
     * Upsert record with some custom error handling
     * 
     * @param p the Product record to upsert
     */
    public void upsert( Product p ) {
        
        conn.generateStatement().setBlocking( true ).upsert( 
            p, 
            e -> {
                throw new RuntimeException("Caught CosmosDB exception", e);
        } );
    }
    
    /**
     * Delete a product
     * 
     * Deletes are more of a pain because you have to delete via document links, rather than primary key of record.
     * The only way to get a documnet link is to have the Document object, which requires a SELECT before DELETE to
     * get the document _selfLink
     * 
     * We can provide generic delete methods for all DAOs though that simply take a collection name and doc id as 
     * the delete link can be constructed from these two items if you also have the db name.
     * 
     * @param p the Product to delete
     */
    public void delete( Product p ) {
        
        Document d = find( p.getId() );
        if ( d != null ) {
            conn.generateStatement().setBlocking( true ).delete( d );
        }
    }
}
