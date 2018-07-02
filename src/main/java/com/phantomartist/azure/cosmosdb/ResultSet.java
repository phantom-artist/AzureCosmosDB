package com.phantomartist.azure.cosmosdb;

import java.util.List;
import java.util.Map;

/**
 * Title: ResultSet
 *
 * Description: Represents a result set from CosmosDB
 */
public interface ResultSet<T> {

    /**
     * Get a single result
     * 
     * @return T the result
     */
    T getSingleResult();
    
    /**
     * Get a list of results
     * 
     * @return List of T the results
     */
    List<T> getResults();
    
    /**
     * Get single result as a JSON string.
     * 
     * @return String the JSON string
     */
    String getSingleResultAsJson();
    
    /**
     * Get a list of results as JSON strings.
     * 
     * @return List of JSON strings
     */
    List<String> getResultsAsJson();
    
    /**
     * Get a single result as a Map of values.
     * 
     * @return Map of values
     */
    Map<Object,Object> getSingleResultAsMap();
    
    /**
     * Get the list of results as Maps
     * 
     * @return List of Maps
     */
    List<Map<Object,Object>> getResultsAsMap();
}
