package com.phantomartist.azure.cosmosdb.results;

/**
 * Title: OnComplete
 *
 * Description: Action fired to signal that a query or statement is complete
 */
public interface OnComplete {

    /**
     * Fired to indicate the completion of a query or statement
     */
    void onComplete();
}
