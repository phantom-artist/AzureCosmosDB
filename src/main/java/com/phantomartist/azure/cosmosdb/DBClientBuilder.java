package com.phantomartist.azure.cosmosdb;

/**
 * Title: DBClientBuilder
 *
 * Description: Builds clients for connecting to CosmosDB.
 * 
 * Typically only 1 client is required per-JVM as connection management is shared and threadsafe,
 * so this class will likely only be created once in a 'startup' method and the resulting object shared around
 * the application.
 */
public interface DBClientBuilder {

    /**
     * Returns a CosmosDBConnection with default throttling limits applied. (10 retry attempts and 30 sec max wait time).
     * 
     * @param endpoint the endpoint of the CosmosDB service (e.g. https://mycosmosdb.documents.azure.com)
     * @param masterKey the master key used to access the database
     * 
     * @return CosmosDBAsyncClient the client
     */
    DBClient getDefaultCosmosDBAsyncClient( 
        final String endpoint, 
        final String masterKey );

    /**
     * Returns a CosmosDBAsyncClient with specified throttling limits applied.
     * 
     * @param endpoint the endpoint of the CosmosDB service (e.g. https://mycosmosdb.documents.azure.com)
     * @param masterKey the master key used to access the store
     * @param maxRetryAttemptsOnThrottledRequests how many times to retry if client detects a throttling response
     * @param maxRetryWaitTimeInSeconds the max cumulative wait time allowed for all retry requests before raising an error
     * 
     * @return CosmosDBAsyncClient the client
     */
    DBClient getCosmosDBAsyncClientWithLimits( 
        final String endpoint, 
        final String masterKey, 
        final int maxRetryAttemptsOnThrottledRequests, 
        final int maxRetryWaitTimeInSeconds );
}
