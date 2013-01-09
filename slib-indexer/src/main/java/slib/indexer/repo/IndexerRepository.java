/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package slib.indexer.repo;

import slib.indexer.IndexHash;

/**
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public class IndexerRepository {
    
    private IndexerRepository repo;

    IndexHash index;
    
    
    private IndexerRepository() {
        index = new IndexHash();
    }

    /**
     *
     * @return
     */
    public IndexerRepository getInstance(){
        
        if(repo == null){
            repo = new IndexerRepository();
        }
        return repo;
    }

    /**
     *
     * @return
     */
    public IndexHash getIndex() {
        return index;
    } 
}
