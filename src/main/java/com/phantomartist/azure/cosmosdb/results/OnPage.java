package com.phantomartist.azure.cosmosdb.results;

import java.util.List;

import com.phantomartist.azure.cosmosdb.Document;

/**
 * Title: OnPage
 *
 * Description: Action to perform when a page of results is returned from the database
 */
public interface OnPage {

    /**
     * Method executed when a page of results is recieved from the DB client.
     * Unless otherwise specified, a page of results is max. 1000 items. so for 
     * very large results sets, this method will be called numerous times.
     * 
     * Typical use-case would be to incrementally build a total collection of 
     * results by converting the page of results in this method to a usable 
     * object type and adding the objects to an overall final Collection
     * that is declared outside of this method scope.
     * 
     * WARNING: Do not implement complicated computation in this method as
     * it could cause backpressure to build in the client that is feeding 
     * the results.
     * 
     * @param results the results
     */
    void onPage( List<Document> results );
}
