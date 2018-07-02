package com.phantomartist.azure.cosmosdb.impl;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.FeedOptions;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.PartitionKey;
import com.microsoft.azure.cosmosdb.SqlParameter;
import com.microsoft.azure.cosmosdb.SqlParameterCollection;
import com.microsoft.azure.cosmosdb.SqlQuerySpec;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.phantomartist.azure.cosmosdb.Query;
import com.phantomartist.azure.cosmosdb.results.OnComplete;
import com.phantomartist.azure.cosmosdb.results.OnError;
import com.phantomartist.azure.cosmosdb.results.OnPage;

import rx.Observable;

/**
 * Title: QueryImpl
 *
 * Description: QueryImpl
 */
public class QueryImpl extends AbstractDBInteraction implements Query {
    
    private static final Logger LOG = LoggerFactory.getLogger( QueryImpl.class );

    private AsyncDocumentClient asyncClient;
    private String collectionLink;
    private String query;
    private SqlParameterCollection sqlParams;
    private int maxFetchResults = 1000; // Default limit
    private boolean isBlocking;
    private String partitionKey;
    
    /**
     * Constructor
     * 
     * @param asyncClient the client
     * @param collectionLink the collection link
     * @param query the query
     */
    public QueryImpl( final AsyncDocumentClient asyncClient, final String collectionLink, final String query ) {
        this.asyncClient = asyncClient;
        this.collectionLink = collectionLink;
        this.query = query;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Query setBlocking( boolean isBlocking ) {
        this.isBlocking = isBlocking;
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Query setMaxResultsPageSize( int maxFetchResults ) {
        this.maxFetchResults = maxFetchResults;
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Query setPartitionKey( String partitionKey ) {
        this.partitionKey = partitionKey;
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Query addParam( final String name, final Object value ) {
        
        if ( sqlParams == null ) {
            sqlParams = new SqlParameterCollection();
        }
        sqlParams.add( new SqlParameter( name, value ) );
        
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute( final OnPage onPage ) {
        execute( onPage, null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute( final OnPage onPage, final OnError onError ) {
        execute( onPage, onError, null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void execute( final OnPage onPage, final OnError onError, final OnComplete onComplete ) {
        executeForResultSet( onPage, onError, onComplete, 
            isBlocking ? 
                new CountDownLatch(1) : 
                null );
    }

    private Observable<FeedResponse<Document>> buildQuery() {
        
        // Build the query
        SqlQuerySpec sql = new SqlQuerySpec( query );
        if ( sqlParams != null ) {
            sql.setParameters( sqlParams );
        }
        
        if ( LOG.isDebugEnabled() ) {
            LOG.debug( "Executing..." + sql );
        }
        
        // Narrow the query to a specific partition?
        FeedOptions queryOptions = new FeedOptions();
        if ( partitionKey != null ) {
            queryOptions.setPartitionKey( new PartitionKey( partitionKey ) );
        } else {
            queryOptions.setEnableCrossPartitionQuery( true );
        }
        
        // Set the page size of this query
        if ( maxFetchResults >= 0 ) {
            queryOptions.setMaxItemCount( maxFetchResults );
        }

        return asyncClient.queryDocuments( 
            collectionLink, 
            sql, 
            queryOptions );
    }
    
    private void executeForResultSet( final OnPage onPage, final OnError onError, final OnComplete onComplete, final CountDownLatch latch ) {
        
        // Only onPage is required, other params are optional
        if ( onPage == null ) {
            throw new IllegalArgumentException( "Must implement OnPage for result set handling" );
        }
        
        if ( LOG.isTraceEnabled() ) {
            LOG.trace( "Entering execute() with latch " + latch );
        }
        
        buildQuery().subscribe( 
            pageResults -> { 
                if ( LOG.isDebugEnabled() ) {
                    LOG.debug( getSQLDebug( query, sqlParams ) + " cost [" + pageResults.getRequestCharge() + "] RU" );
                }
                
                // Wrap the Azure objects and do the user's work
                onPage.onPage( DocumentUtil.wrap( pageResults.getResults() ) );
            }, 
            e -> { 
                try {
                    LOG.error( getSQLDebug( query, sqlParams ) + "] caused " + e.getMessage(), e);
                    if ( onError != null ) {
                        onError.onError( e );
                    }
                } finally {
                    countDown( latch );  // Must call to prevent extended blocking
                }
            }, 
            () -> {
                try {
                    if ( LOG.isDebugEnabled() ) {
                        LOG.debug( "Complete" );
                    }
                    if ( onComplete != null ) {
                        onComplete.onComplete();
                    }
                } finally {
                    countDown( latch );  // Must call to prevent extended blocking
                }
            });
        
        await( latch );

        if ( LOG.isTraceEnabled() ) {
            LOG.trace( "Exiting execute()" );
        }
    }
    
    private String getSQLDebug( String sql, SqlParameterCollection sqlParams ) {
        
        StringBuffer debug = new StringBuffer( sql );
        if ( sqlParams != null ) {
            debug.append( " " );
            for ( SqlParameter param : sqlParams ) {
                debug.append( param.toJson() );
            }
        }
        
        return debug.toString();
    }
}
