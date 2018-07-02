package com.phantomartist.azure.cosmosdb.results;

import com.phantomartist.azure.cosmosdb.Document;

/**
 * Title: OnResult
 *
 * Description: Action to execute when a single result is returned
 */
public interface OnResult {

    /**
     * Exceute this function when single result is received.
     * 
     * @param result the result - for delete calls <code>result</code> will always be <code>null</code>
     */
    void onResult( Document result );
}
