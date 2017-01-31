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
public interface Document {
    
    public String getId();
    
    /**
     * @return load the content into memory and return it
     */
    public String getContent();
    
}
