package com.phantomartist.azure.cosmosdb.impl;

import com.phantomartist.azure.cosmosdb.Document;

/**
 * Title: DocumentImpl
 *
 * Description: Wrapper for Azure Document
 */
public class DocumentImpl implements Document {

    private com.microsoft.azure.cosmosdb.Document azureDoc;
    
    /**
     * Constructor
     * 
     * @param azureDoc the Azure document
     */
    public DocumentImpl( com.microsoft.azure.cosmosdb.Document azureDoc ) {
        if ( azureDoc == null ) {
            throw new IllegalArgumentException( "AzureDoc cannot be null" );
        }
        this.azureDoc = azureDoc;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {

        return azureDoc.getId();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getSelfLink() {

        return azureDoc.getSelfLink();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toJson() {
        
        return azureDoc.toJson();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T toObject( Class<T> c ) {
        
        return azureDoc.toObject( c );
    }

}
