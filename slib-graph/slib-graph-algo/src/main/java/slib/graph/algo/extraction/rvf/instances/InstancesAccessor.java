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
package slib.graph.algo.extraction.rvf.instances;

import java.util.Set;
import org.openrdf.model.URI;

/**
 *
 * Interface defining a access to the instances of the classes
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public interface InstancesAccessor {

    /**
     * Return a way to iterate over the collection of instances. The definition
     * of instance is domain specific and depends on the use case.
     *
     * @return a way to iterate over the collection of instances
     */
    public Iterable<URI> getInstances();

    /**
     * Access to a way to iterate over an instance of a class. This method must
     * provide the inferred results, i.e. if x is a subclass of y and we ask for
     * the instance of y, this method will also return all the instances typed
     * to x as a result. see to {@link #getDirectInstances(org.openrdf.model.URI)
     * } for an access to the direct classes.
     *
     * @param v the class of interest
     * @return the set of instances of the specified class
     */
    public Iterable<URI> getInstances(URI v);

    /**
     * Access to a way to iterate through the set of instances considered as
     * direct instances of a class e.g. all x respecting x RDF.TYPE class
     * considering that a transitive reduction have been performed to remove
     * inferable RDF.TYPE relationships.
     *
     * @param v the class of interest
     * @return the set of direct instances of the specified class
     */
    public Iterable<URI> getDirectInstances(URI v);

    /**
     * Method providing access to the classes annotating an instance. This method
     * doesn't support inference, i.e. considering an instance y only the vertex
     * x for which y RDF.TYPE x are returned.
     * 
     *
     * @param v the vertex corresponding to the instance
     * @return the set of classes associated to the instance
     */
    public Set<URI> getDirectClass(URI v);
}
