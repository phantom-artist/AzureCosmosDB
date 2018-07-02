package com.phantomartist.azure.cosmosdb.impl;

import java.util.ArrayList;
import java.util.List;

import com.phantomartist.azure.cosmosdb.Document;

/**
 * Title: DocumentUtil
 *
 * Description: Document utilities 
 */
public class DocumentUtil {

    private DocumentUtil() {
    }
    
    /**
     * Convert a Json String into a Document object.
     * 
     * @param json the JSON
     * 
     * @return Document the document
     */
    public static Document fromJson( String json ) {
        return new DocumentImpl( new com.microsoft.azure.cosmosdb.Document( json ) );
    }
    
    /**
     * Wrap MS Azure Document in our own flavour to avoid exposing underlying implementation.
     * 
     * @param results the results
     * 
     * @return List of our own Document type.
     */
    public static List<Document> wrap( List<com.microsoft.azure.cosmosdb.Document> results ) {
        
        List<Document> wrapped = new ArrayList<>( results.size() );
        for ( com.microsoft.azure.cosmosdb.Document doc : results ) {
            wrapped.add( wrap( doc ) );
        }
        return wrapped;
    }
    
    /**
     * Wrap an Azure document in a Document object.
     * 
     * @param doc the Azure document
     * 
     * @return Document the wrapper
     */
    public static Document wrap( com.microsoft.azure.cosmosdb.Document doc ) {
        return new DocumentImpl( doc );
    }
}
