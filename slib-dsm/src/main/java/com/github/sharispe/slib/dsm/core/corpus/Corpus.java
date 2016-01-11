/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.sharispe.slib.dsm.core.corpus;

/**
 *
 * @author sharispe
 */
public interface Corpus {
    
    /**
     * @return the size of the corpus
     */
    public long getSize();
    
    /**
     * @return the document as an Iterable
     */
    public Iterable<Document> getDocuments();
    
    
    
}
