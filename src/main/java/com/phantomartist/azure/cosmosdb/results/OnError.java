package com.phantomartist.azure.cosmosdb.results;

/**
 * Title: OnError
 *
 * Description: Action fired when an error occurs in a query or statement
 */
public interface OnError {

    /**
     * Errors thrown by underlying DB client will be propagated through this method.
     * 
     * @param t the throwable
     */
    void onError( Throwable t );
}
