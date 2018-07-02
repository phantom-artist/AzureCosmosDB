package com.phantomartist.azure.cosmosdb.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.phantomartist.azure.cosmosdb.Connection;
import com.phantomartist.azure.cosmosdb.DBClient;
import com.phantomartist.azure.cosmosdb.Document;

/**
 * Title: CosmosDBAPITest
 *
 * Description: Tests were run against a CosmosDB Emulator (Windows 2016 Server)
 * 
 * JUnit 5 tests
 * 
 * This test shows-off both the blocking and non-blocking API. 
 * In order for the Async tests to complete, we have to artificially 'block' the calls with latches.
 * 
 * Tests also show conversion of results to Object types (Map + Custom POJO).
 */
class CosmosDBAPITest {

    private static final String SERVICE_ENDPOINT = "<service endpoint>"; // Replace with your values
    private static final String MASTER_KEY = "<key for accessing Azure CosmosDB>"; // Replace with your values
    
    private static final String DB = "mydb";
    private static final String COLLECTION = "product";
    
    private DBClient client;
    private Connection conn;
    
    @BeforeEach
    void before() {
        
        // Client and Connection can be re-used across JVM for all queries against 
    	// /dbs/mydb/colls/product collection link
        client = DBClientBuilderImpl.getDefault().getDefaultCosmosDBAsyncClient( SERVICE_ENDPOINT, MASTER_KEY );
        conn = client.getConnection( DB, COLLECTION );
        
        final List<Document> docsToDelete = new ArrayList<>();
        
        // Select all docs
        conn.generateQuery( "SELECT * FROM product" )
            .setBlocking( true )
            .execute( page -> {
                docsToDelete.addAll( page );
            }, e -> {
                fail( "Unable to select all docs for test setup" );
            });
        
        for ( Document doc : docsToDelete ) {

            // Delete all docs
            conn.generateStatement()
                .setBlocking( true )
                .delete( doc, e -> { fail("Unable to delete all docs for test setup" ); });
        }
    }
    
    @AfterEach
    void after() {
        
        client.close();
    }
    
    /**
     * Test an asynchronous query
     * 
     * We use an artificial countdown latch to prevent the test exiting before the query completes.
     * 
     * @throws InterruptedException
     */
    @Test
    void testAsyncQuery() throws InterruptedException {

        // Add records for test
        multiUpsert();
        
        // Async query requires a manual latch to prevent test exiting before result is read
        final CountDownLatch latch = new CountDownLatch(1);
        
        // NB: Generated Queries are not threadsafe
        conn.generateQuery( "SELECT * FROM product WHERE product.id = @id" )
            .addParam( "@id", "record_1" )
            .execute(
                results -> {
                    
                    List<Product> products = new ArrayList<Product>();
                    for ( Document doc : results ) {
                        products.add( doc.toObject( Product.class ) );
                    }
                    assertEquals( 1, products.size() );
                    latch.countDown();
                },
                e -> {
                    e.printStackTrace();
                    fail( "Query errored" );
                    latch.countDown();
                },
                () -> {
                    assertTrue( true ); // Nothing really useful to do here
                }
            );
        
        // Do something with products list
        
        latch.await( 5000, TimeUnit.SECONDS ); // Timeout after 5 secs if no result retrieved
    }

    /**
     * Test the same query, this time as a blocking query (no need to add manual latches).
     * 
     * execute() will not complete before onError or onComplete complete
     */
    @Test
    void testBlockingQuery() {
        
        // Add records for test
        multiUpsert();
        
        // NB: Queries are not threadsafe
        conn.generateQuery( "SELECT * FROM product WHERE product.id = @id" )
            .setBlocking( true )  // Make this a blocking query (no separate latch required)
            .addParam( "@id", "record_1" )
            .execute(
                results -> {
                    
                    List<Product> products = new ArrayList<Product>();
                    for ( Document doc : results ) {
                        products.add( doc.toObject( Product.class ) );
                    }
                    assertEquals( 1, products.size() );
                },
                e -> {
                    e.printStackTrace();
                    fail( "Query errored" );
                },
                () -> {
                    assertTrue( true ); // Nothing really useful to do here
                }
            );
    }
    
    /**
     * Test upserting a Map as opposed to a POJO
     */
    @Test
    public void testUpsertMap() {
        
        Map<String,String> map = new HashMap<>();
        map.put( "id", "map_1");
        map.put( "desc", "description" );
        
        conn.generateStatement()
            .setBlocking( true )
            .upsert( map, 
                e -> { 
                    e.printStackTrace();
                    fail("Failed testUpsertMap");  
                } );
        
        conn.generateQuery( "SELECT * FROM product WHERE product.id = @id" )
            .setBlocking( true )
            .addParam( "@id", "map_1" )
            .execute( page -> {
                assertEquals( "description", page.get(0).toObject( HashMap.class ).get( "desc" ) );
            }, e -> {
                e.printStackTrace();
                fail( "Failed testUpsertMap" );
            });
    }
    
    /**
     * Test upsert of multiple documents. Backend logging aggregates RU usage across the batch.
     */
    @Test
    void testMultiUpsert() {
        
        List<Product> records = getTestRecords();
        
        conn.generateStatement()
            .setBlocking( true )
            .multiUpsert(
                records, 
                e -> {
                    e.printStackTrace();
                    fail( "Failed multi-upsert");
                }, () -> {
                    assertTrue( true ); // Nothing to really do here
                });
    }
    
    /**
     * Upsert and delete a document
     */
    @Test
    void testUpsertAndDelete() {
        
        Product doc = new Product();
        doc.setId( "record_3" );

        conn.generateStatement()
            .setBlocking( true )
            .upsert( doc, 
               r -> {
                  assertEquals( "record_3", r.getId() ); 
            }, e -> {
                e.printStackTrace();
                fail( "Failed upsert");
            }, () -> {
                assertTrue( true ); // Nothing to really do here
            });
        
        final List<Document> toDelete = new ArrayList<>();
        
        // Find the record we just added
        conn.generateQuery( "SELECT * FROM Product WHERE Product.id = @id" )
           .setBlocking( true )
           .addParam( "@id", "record_3" )
           .execute(
               rr -> {           
                   toDelete.add( rr.get( 0 ) );
               },
               e -> {
                   e.printStackTrace();
                   fail("Failed to find record");
               }, () -> {
                   assertTrue( true );
               });
        
        // Delete the record we just found
        conn.generateStatement()
            .setBlocking( true )
            .delete( toDelete.get(0), 
                e -> { 
                    fail("Failed to delete"); 
                }, 
                () -> { 
                    assertTrue( true ); 
                } );
        
        // Search for it again and find nothing
        conn.generateQuery( "SELECT * FROM Product WHERE Product.id = @id" )
            .setBlocking( true )
            .addParam( "@id", "record_3" )
            .execute( page -> {
                assertTrue( page.isEmpty() );
            }, e -> {
                fail("Failed to find"); 
            });
    }
    
    /**
     * This test demonstrates how to page results to the onPage() in batches, 
     * rather than returning the entire result set in one go.
     */
    @Test
    void testPaging() {
        
        // Ensure 2 records are present
        multiUpsert();

        final AtomicInteger pageCount = new AtomicInteger(0);

        // Page over 2 results and assert the result of 2 iterations of page
        conn.generateQuery( "SELECT * FROM Product" )
           .setBlocking( true )
           .setMaxResultsPageSize( 1 ) // Limit the page size to 1 result at a time
           .execute( 
               page -> {           
                   pageCount.incrementAndGet();
               }, e -> {
                   e.printStackTrace();
                   fail("Failed to find record");
               }, () -> {
                   assertEquals( 2, pageCount.get() ); // Assert we got called twice
               });
    }
    
    /**
     * Typical use-case where we want to populate a Collection of results from a query
     * and then use those results in further processing. Requires a blocking query.
     */
    @Test
    void testPopulation() {
        
        // Ensure 2 records are present in DB
        multiUpsert();

        // Empty collection to hold the results
        final List<Document> results = new ArrayList<>();

        // Use a blocking query to page over results and add each page to results collection
        conn.generateQuery( "SELECT * FROM Product" )
           .setBlocking( true )            // Block until onComplete executes
           .setMaxResultsPageSize( 10 )
           .execute( 
               page -> {
                   results.addAll( page ); // Add a page of results to the results collection
               }, e -> {
                   e.printStackTrace();
                   fail( "Failed to find records" );
               }, () -> {
                   assertEquals( 2, results.size() ); // Assert we got 2 records when onComplete fires
               });
        
        // Use fully-populated results in further processing
        // (Will not get called until blocking execute() completes)
        assertEquals( 2, results.size() );
    }

    private void multiUpsert() {
        
        List<Product> records = getTestRecords();
        
        conn.generateStatement()
            .setBlocking( true )
            .multiUpsert(
                records, 
                e -> {
                    e.printStackTrace();
                    fail( "Failed multi-upsert");
                }, () -> {
                    assertTrue( true ); // Nothing to really do here
                });
    }
    
    /**
     * @return List of Test Records
     */
    private List<Product> getTestRecords() {
        
        Product record1 = new Product();
        record1.setId( "record_1");
        record1.setCreatedDate( new Date());
        record1.setCreatedTime( new Timestamp( System.currentTimeMillis() ));
        record1.setDesc( "First record");
        record1.setCount( 12345 );
        record1.setRefId( 999 );
        record1.setUuid( UUID.randomUUID() );
        record1.setFindable( true );
        
        Product record2 = new Product();
        record2.setId( "record_2" );
        record2.setCreatedDate( new Date() );
        record2.setCreatedTime( new Timestamp( System.currentTimeMillis() ) );
        record2.setDesc( "Second record");
        record2.setCount( 123456 );
        record2.setRefId( 777 );
        record2.setUuid( UUID.randomUUID() );
        record2.setFindable( false );
        
        List<Product> records = new ArrayList<>();
        records.add(record1);
        records.add(record2);
        
        return records;
    }
}
