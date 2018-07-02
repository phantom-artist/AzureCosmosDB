package com.phantomartist.azure.cosmosdb;

import java.util.List;

import com.phantomartist.azure.cosmosdb.results.OnComplete;
import com.phantomartist.azure.cosmosdb.results.OnError;
import com.phantomartist.azure.cosmosdb.results.OnResult;

/**
 * Title: Statement
 *
 * Description: Perform a modification on the database
 */
public interface Statement {

    /**
     * Set the blocking flag. (Default is false).
     * If set to true, the statement action (upsert/delete) 
     * will block until completion rather than returning immediately.
     * 
     * @param isBlocking if true, statement blocks until completion
     * 
     * @return Statement this statement
     */
    Statement setBlocking( final boolean isBlocking );
    
    /**
     * Perform an insert/update for a given document. 
     * 
     * Assume default actions for page, error and completion.
     * 
     * @param doc the doc
     */
    void upsert( final Object doc );
    
    /**
     * Perform an insert/update for a given document. 
     * 
     * Assumes default actions for result and completion.
     * 
     * @param doc the doc
     * @param onError what to do if an error occurs
     */
    void upsert( final Object doc, final OnError onError );
    
    /**
     * Perform an insert/update for a given document. 
     * 
     * @param doc the doc
     * @param onError what to do if an error occurs
     * @param onComplete what to do when the call completes
     */
    void upsert( final Object doc, final OnError onError, final OnComplete onComplete );
    
    /**
     * Perform an insert/update for a given document.
     * Pass actions for OnResult, OnError and OnComplete.
     * 
     * @param doc the doc
     * @param onResult what to do when a result is observed
     * @param onError what to do if an error occurs
     * @param onComplete what to do when the call completes
     */
    void upsert( final Object doc, final OnResult onResult, final OnError onError, final OnComplete onComplete );
    
    /**
     * Perform multiple insert/update operation
     * 
     * @param docs the list of docs to insert/update
     * @param onError what to do if an error occurs
     * @param onComplete what to do when the call completes
     */
    void multiUpsert( final List<? extends Object> docs, final OnError onError, final OnComplete onComplete );
    
    /**
     * Perform multiple insert/update operation
     * 
     * @param docs the list of docs to insert/update
     * @param onResult what to do when a result is observed
     * @param onError what to do if an error occurs
     * @param onComplete what to do when the call completes
     */
    void multiUpsert( final List<? extends Object> docs, final OnResult onResult, final OnError onError, final OnComplete onComplete );
    
    /**
     * Delete a document.
     * 
     * WARNING: This API provides the caller no control over error handling or completion actions.
     * Default logging and blocking behaviour will apply. Only use this if you require minimal
     * logging behaviour for errors and don't care about the result of the operation.
     * 
     * @param docLink the document to delete
     */
    void delete( final Document docLink );
    
    /**
     * Delete a document.
     * 
     * Assumes default behaviour for paging and completion with a custom error handler
     * 
     * @param docLink the document to delete
     * @param onError what to do if an error occurs
     */
    void delete( final Document docLink, final OnError onError );
    
    /**
     * Delete a document
     * 
     * Assumes default behaviour for paging with a custom error handler and completion action.
     * 
     * @param docLink containing the doc link
     * @param onError what to do if an error occurs
     * @param onComplete what to do when the call completes
     */
    void delete( final Document docLink, final OnError onError, final OnComplete onComplete );
    
    /**
     * Delete a document
     * 
     * Specify custom actions for paging, error handling and completion actions.
     * 
     * @param docLink the doc
     * @param onResult what to do when a result is observed
     * @param onError what to do if an error occurs
     * @param onComplete what to do when the call completes
     */
    void delete( final Document docLink, final OnResult onResult, final OnError onError, final OnComplete onComplete );
}
