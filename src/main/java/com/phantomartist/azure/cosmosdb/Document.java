package com.phantomartist.azure.cosmosdb;

/**
 * Title: Document
 *
 * Description: Representation of a document 
 */
public interface Document {

    /**
     * Get the _id field of the document
     * 
     * @return String the id field
     */
    String getId();
    
    /**
     * Get the self-referencing unique document link for this document.
     * 
     * @return String the self-link
     */
    String getSelfLink();
    
    /**
     * Convert the Document to a JSON array.
     * 
     * @return String the JSON representation
     */
    String toJson();
    
    /**
     * Convert the Document to an object of type T
     * 
     * @param <T> the type of the object.
     * @param c   the class of the object, either a POJO class or JSONObject. If c is a POJO class, it must be a member
     *            (and not an anonymous or local) and a static one.
     *            
     * @return the object of type T
     */
    <T extends Object> T toObject( Class<T> c );
}
