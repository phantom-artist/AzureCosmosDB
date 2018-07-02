package com.phantomartist.azure.cosmosdb.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.phantomartist.azure.cosmosdb.Document;

/**
 * Title: CosmosDBDAOCloudTest
 *
 * Description: Demonstrates how to use a DAO-style approach using CosmosDB API
 */
@Disabled( "For illustration purposes only")
class CosmosDBDAOTest {

    @SuppressWarnings({ "unchecked", "unused" })
    @Test
    void testDAO() {
        
        // Create a DAO class per-collection.
        CosmosDBDAO dao = new CosmosDBDAO();
        
        // Find a record
        Document doc = dao.find( "record_1" );
        
        // Convert the record to a structure or POJO class
        Map<String,String> convertedToObject = doc.toObject( HashMap.class );
        
        // Find multiple/all documents
        List<Document> allDocs = dao.findAll();
    }

}
