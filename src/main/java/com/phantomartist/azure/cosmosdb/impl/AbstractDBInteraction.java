package com.phantomartist.azure.cosmosdb.impl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Title: AbstractDBInteraction
 *
 * Description: Abstract methods for CosmosDB interaction classes
 */
public abstract class AbstractDBInteraction {

    /**
     * Perform a countdown if the latch is not null
     * 
     * @param latch the latch
     */
    protected void countDown( final CountDownLatch latch ) {
        if ( latch != null ) {
            latch.countDown();
        }
    }
    
    /**
     * Await the latch completion
     * 
     * @param latch the latch
     */
    protected void await( final CountDownLatch latch ) {
        if ( latch == null ) {
            return;
        }
        try {
            latch.await( 60, TimeUnit.MINUTES ); // TODO: Should this be configurable? Default 1hr timeout
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while awaiting latch countdown",e);
        }
    }
}
