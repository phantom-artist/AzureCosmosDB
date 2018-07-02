package com.phantomartist.azure.cosmosdb.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.azure.cosmosdb.RequestOptions;
import com.microsoft.azure.cosmosdb.ResourceResponse;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import com.phantomartist.azure.cosmosdb.Document;
import com.phantomartist.azure.cosmosdb.Statement;
import com.phantomartist.azure.cosmosdb.results.OnComplete;
import com.phantomartist.azure.cosmosdb.results.OnError;
import com.phantomartist.azure.cosmosdb.results.OnResult;

import rx.Observable;

/**
 * Title: StatementImpl
 *
 * Description: Statement implementation that performs a modification on the database
 */
public class StatementImpl extends AbstractDBInteraction implements Statement {

    private static final Logger LOG = LoggerFactory.getLogger( StatementImpl.class );
    
    private AsyncDocumentClient asyncClient;
    private String collectionLink;
    private boolean isBlocking;
    
    /**
     * Constructor
     *
     * @param asyncClient asyncClient the client
     * @param collectionLink the collection link that we're operating on
     */
    public StatementImpl( final AsyncDocumentClient asyncClient, final String collectionLink ) {
        this.asyncClient = asyncClient;
        this.collectionLink = collectionLink;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Statement setBlocking( boolean isBlocking ) {
        this.isBlocking = isBlocking;
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void upsert( final Object doc ) {
        upsert( doc, null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void upsert( final Object doc, final OnError onError ) {
        upsert( doc, onError, null ); 
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void upsert( final Object doc, final OnError onError, final OnComplete onComplete ) {
        upsert( doc, null, onError, onComplete );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void upsert( final Object doc, final OnResult onResult, final OnError onError, final OnComplete onComplete ) {
        upsert( doc, null, onError, onComplete, 
            isBlocking ? 
                new CountDownLatch( 1 ) : 
                null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void multiUpsert( final List<? extends Object> docs, final OnError onError, final OnComplete onComplete ) {
        multiUpsert( docs, null, onError, onComplete );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void multiUpsert( final List<? extends Object> docs, final OnResult onResult, final OnError onError, final OnComplete onComplete ) {
        multiUpsert( docs, onResult, onError, onComplete, 
            isBlocking ? 
                new CountDownLatch( 1 ) : 
                null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void delete( final Document docLink) {
        delete( docLink, null );
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete( final Document docLink, OnError onError ) {
        delete( docLink, null, null );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void delete( final Document docLink, final OnError onError, final OnComplete onComplete) {
        delete( docLink, null, onError, onComplete );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete( final Document docLink, final OnResult onResult, final OnError onError, final OnComplete onComplete ) {
        delete( docLink, onResult, onError, onComplete,
            isBlocking ? 
                new CountDownLatch( 1 ) : 
                null );
    }
    
    /**
     * Perform the upsert in either asynchronous or blocking manner.
     * 
     * @param doc the document to upsert
     * @param onResult action to execute on result
     * @param onError action to execute on error
     * @param onComplete action to execute on complete
     * @param latch the latch (null if async execution)
     */
    private void upsert( final Object doc, final OnResult onResult, final OnError onError, final OnComplete onComplete, final CountDownLatch latch ) {
        
        createUpsert( doc ).subscribe(
            rr -> {
                
                Document wrapped = DocumentUtil.wrap( rr.getResource() );
                if ( LOG.isDebugEnabled() ) {
                    LOG.debug( "Upsert of doc [" + wrapped.getId() + "] cost [" + rr.getRequestCharge() + "]" );
                }
                if ( onResult != null ) {
                    onResult.onResult( wrapped );
                }
                
            }, e -> {
                
                try {
                    LOG.error( "Error during upsert for doc [" + doc + "]", e );
                    if ( onError != null ) {
                        onError.onError( e );
                    }
                } finally {
                    countDown( latch ); // Must call to prevent extended blocking
                }
                
            }, () -> {
                
                try {
                    if ( LOG.isDebugEnabled() ) {
                        LOG.debug( "Upsert of doc [" + doc + "] complete" );
                    }
                    if ( onComplete != null ) {
                        onComplete.onComplete();
                    }
                } finally {
                    countDown( latch ); // Must call to prevent extended blocking
                }
            });
        await( latch );
    }

    /**
     * Perform the multi-upsert in either asynchronous or blocking mode.
     * 
     * @param docs the docs
     * @param onResult action to perform on result
     * @param onError action to perfom on error
     * @param onComplete action to perform on complete
     * @param latch the latch (null if async execution)
     */
    private void multiUpsert( final List<? extends Object> docs, final OnResult onResult, final OnError onError, final OnComplete onComplete, final CountDownLatch latch ) {
        
        // Register a list of observables
        List<Observable<ResourceResponse<com.microsoft.azure.cosmosdb.Document>>> createDocumentsOBs =
            new ArrayList<>();
        
        for ( Object doc : docs ) {            
            createDocumentsOBs.add( createUpsert( doc ) );
        }

        // Reduce the total RU charge for the batch operation
        Observable.merge(createDocumentsOBs)
            .map(ResourceResponse::getRequestCharge)
            .reduce((sum, value) -> sum+value)
            .subscribe( totalRU -> {
                
                if ( LOG.isDebugEnabled() ) {
                    LOG.debug( "Multi-Upsert [" + docs.size() + "] docs, average cost [" + totalRU / docs.size() + "] total cost [" + totalRU + "]");
                }

            }, e -> {
                
                try {
                    LOG.error( "Error occurred while calculating total RU for multi-upsert", e);
                    if ( onError != null ) {
                        onError.onError( e );
                    }
                } finally {
                    countDown( latch );  // Must call to prevent extended blocking
                }
                
            }, () -> {
                
                try {
                    if ( LOG.isDebugEnabled() ) {
                        LOG.debug( "Multi-Upsert [" + docs.size() + "] docs, complete" );
                    }
                    if ( onComplete != null ) {
                        onComplete.onComplete();
                    }
                } finally {
                    countDown( latch );  // Must call to prevent extended blocking
                }
            });
        await( latch );
    }
    
    /**
     * Perform the delete in asynchronous or blocking mode.
     * 
     * @param docLink the doc to delete
     * @param onResult action to perform on result
     * @param onError action to perform on error
     * @param onComplete action to perform on complete
     * @param latch the latch (null if asynchronous)
     */
    private void delete( final Document docLink, final OnResult onResult, final OnError onError, final OnComplete onComplete, final CountDownLatch latch ) {
        
        createDelete( docLink ).subscribe( rr -> {
            
            if ( LOG.isDebugEnabled() ) {
                LOG.debug( "Deleting doc id [" + docLink.getId() + "] with link [" + docLink.getSelfLink() + "] cost [" + rr.getRequestCharge() + "]" );
            }
            // TODO: rr.getResource() is null after a delete, so calling this is a bit pointless
            if ( onResult != null ) {
                onResult.onResult( null );
            }
        }, e -> {
            
            try {
                LOG.error( "Error during delete for doc [" + docLink.getId() + "] with link [" + docLink.getSelfLink() + "]", e);
                if ( onError != null ) {
                    onError.onError( e );
                }
            } finally {
                countDown( latch );  // Must call to prevent extended blocking
            }
            
        }, () -> {
            
            try {
                if ( LOG.isDebugEnabled() ) {
                    LOG.debug( "Delete [" + docLink.getId() + "] with link [" + docLink.getSelfLink() + "], complete" );
                }
                if ( onComplete != null ) {
                    onComplete.onComplete();
                }
            } finally {
                countDown( latch );  // Must call to prevent extended blocking
            }
        });
        await( latch );
    }

    /**
     * Create the upsert and retrieve the Observable
     * 
     * @param doc the doc
     * 
     * @return Observable the observable
     */
    private Observable<ResourceResponse<com.microsoft.azure.cosmosdb.Document>> createUpsert( final Object doc ) {
        
        return asyncClient.upsertDocument(
            collectionLink, 
            doc, 
            new RequestOptions(), 
            true );
    }
    
    /**
     * Create the delete and retrieve the Observable
     * 
     * @param docLink the doc link
     * 
     * @return Observable the observable
     */
    private Observable<ResourceResponse<com.microsoft.azure.cosmosdb.Document>> createDelete( final Document docLink ) {
     
        return asyncClient.deleteDocument( docLink.getSelfLink(), null );
    }
}
