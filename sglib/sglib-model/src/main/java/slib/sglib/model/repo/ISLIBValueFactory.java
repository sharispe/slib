/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package slib.sglib.model.repo;

import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;

/**
 *
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public interface ISLIBValueFactory extends ValueFactory{
    
    /**
     *
     * @param uri
     * @return
     */
    public URI getURI(URI uri);
}
