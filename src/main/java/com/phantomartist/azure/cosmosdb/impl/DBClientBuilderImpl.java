package com.phantomartist.azure.cosmosdb.impl;

import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.RetryOptions;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.phantomartist.azure.cosmosdb.Connection;
import com.phantomartist.azure.cosmosdb.DBClient;
import com.phantomartist.azure.cosmosdb.DBClientBuilder;

/**
 * Title: DBClientBuilderImpl
 *
 * Description: Default implementation of DBClientBuilder 
 */
public class DBClientBuilderImpl implements DBClientBuilder {

    private static final DBClientBuilderImpl INSTANCE = new DBClientBuilderImpl();
    
    public static DBClientBuilderImpl getDefault() {
        return INSTANCE;
    }
    
    private DBClientBuilderImpl() {
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public DBClient getDefaultCosmosDBAsyncClient( final String endPoint, final String masterKey ) {
        
        return new CosmosDBAsyncDocumentClient( endPoint, masterKey );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DBClient getCosmosDBAsyncClientWithLimits(String endpoint, String masterKey, int maxRetryAttemptsOnThrottledRequests, int maxRetryWaitTimeInSeconds ) {
        
        return new CosmosDBAsyncDocumentClient( endpoint, masterKey, maxRetryAttemptsOnThrottledRequests, maxRetryWaitTimeInSeconds );
    }

    private static class CosmosDBAsyncDocumentClient implements DBClient {

        private AsyncDocumentClient asyncClient;
        private String endPoint;
        private String masterKey;
        
        CosmosDBAsyncDocumentClient( final String endPoint, final String masterKey, int maxRetryAttemptsOnThrottledRequests, int maxRetryWaitTimeInSeconds ) {

            this.endPoint = endPoint;
            this.masterKey = masterKey;
            
            RetryOptions ro = new RetryOptions();
            ro.setMaxRetryAttemptsOnThrottledRequests(maxRetryAttemptsOnThrottledRequests);
            ro.setMaxRetryWaitTimeInSeconds(maxRetryWaitTimeInSeconds);
            
            ConnectionPolicy cp = new ConnectionPolicy();
            cp.setRetryOptions(ro);
            
            asyncClient = new AsyncDocumentClient.Builder()
                .withServiceEndpoint( endPoint )
                .withMasterKey( masterKey )
                .withConnectionPolicy( cp )
                .withConsistencyLevel( ConsistencyLevel.Session )
                .build();
        }
        
        CosmosDBAsyncDocumentClient( final String endPoint, final String masterKey ) {

            this( endPoint, masterKey, 9, 30 ); // These are the MS Azure defaults
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getEndPoint() {
            
            return endPoint;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getMasterKey() {
            
            return masterKey;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Connection getConnection( final String db, final String collection ) {
            
            return new CosmosDBConnectionImpl( asyncClient, db, collection );
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void close() {
            
            asyncClient.close();
        }
        
    }
}
