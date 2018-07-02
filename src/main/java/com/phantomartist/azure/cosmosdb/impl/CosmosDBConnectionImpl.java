package com.phantomartist.azure.cosmosdb.impl;

import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.phantomartist.azure.cosmosdb.Connection;
import com.phantomartist.azure.cosmosdb.Query;
import com.phantomartist.azure.cosmosdb.Statement;

/**
 * Title: CosmosDBConnectionImpl
 *
 * Description: CosmosDBConnectionImpl
 */
public class CosmosDBConnectionImpl implements Connection {

    private AsyncDocumentClient asyncClient;
    private String collectionLink;
    
    /**
     * Constructor
     * 
     * @param asyncClient the document client
     * @param db the db
     * @param collection the collection
     */
    public CosmosDBConnectionImpl( final AsyncDocumentClient asyncClient, final String db, final String collection ) {
        this.asyncClient = asyncClient;
        collectionLink = String.format( "/dbs/%s/colls/%s", db, collection );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getCollectionLink() {

        return collectionLink;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Query generateQuery( final String query ) {
        
        return new QueryImpl( asyncClient, collectionLink, query );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement generateStatement() {
        
        return new StatementImpl( asyncClient, collectionLink );
    }    
}
