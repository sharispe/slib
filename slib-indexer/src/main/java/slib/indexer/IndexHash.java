/* 
 *  Copyright or © or Copr. Ecole des Mines d'Alès (2012-2014) 
 *  
 *  This software is a computer program whose purpose is to provide 
 *  several functionalities for the processing of semantic data 
 *  sources such as ontologies or text corpora.
 *  
 *  This software is governed by the CeCILL  license under French law and
 *  abiding by the rules of distribution of free software.  You can  use, 
 *  modify and/ or redistribute the software under the terms of the CeCILL
 *  license as circulated by CEA, CNRS and INRIA at the following URL
 *  "http://www.cecill.info". 
 * 
 *  As a counterpart to the access to the source code and  rights to copy,
 *  modify and redistribute granted by the license, users are provided only
 *  with a limited warranty  and the software's author,  the holder of the
 *  economic rights,  and the successive licensors  have only  limited
 *  liability. 

 *  In this respect, the user's attention is drawn to the risks associated
 *  with loading,  using,  modifying and/or developing or reproducing the
 *  software by the user in light of its specific status of free software,
 *  that may mean  that it is complicated to manipulate,  and  that  also
 *  therefore means  that it is reserved for developers  and  experienced
 *  professionals having in-depth computer knowledge. Users are therefore
 *  encouraged to load and test the software's suitability as regards their
 *  requirements in conditions enabling the security of their systems and/or 
 *  data to be ensured and,  more generally, to use and operate it in the 
 *  same conditions as regards security. 
 * 
 *  The fact that you are presently reading this means that you have had
 *  knowledge of the CeCILL license and that you accept its terms.
 */
package slib.indexer;

import java.util.HashMap;
import java.util.Map;
import org.openrdf.model.URI;

/**
 * Class used to associate an URI to an object which contains its descriptions
 * as String values. This class is therefore mainly used to store labels or
 * string descriptions of a resource identified by an URI.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class IndexHash {

    Map<URI, URIDescription> mapping = new HashMap<URI, URIDescription>();

    /**
     * Access to the Map which stores the description of each resource.
     *
     * @return the complete mapping
     */
    public Map<URI, URIDescription> getMapping() {
        return mapping;
    }

    /**
     * Access to the description associated to a specific URI
     *
     * @param v the URI of the resource of interest
     * @return the description associated to the given URI or null if the URI is
     * not loaded in the index.
     */
    public URIDescription getDescription(URI v) {
        return mapping.get(v);
    }

    /**
     * Associate a description to the given URI
     *
     * @param x the URI of the resource
     * @param o its description
     */
    public void addDescription(URI x, URIDescription o) {
        mapping.put(x, o);
    }

    /**
     * Return true if the given URI is associated to a description.
     *
     * @param x the URI of the resource
     * @return true is the given URI is indexed
     */
    public boolean containsDescriptionFor(URI x) {
        return mapping.containsKey(x);
    }
}
