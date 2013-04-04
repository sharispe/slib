package slib.sglib.algo.graph.extraction.rvf.instances;

import java.util.Set;
import org.openrdf.model.URI;

/**
 *
 * Interface defining a access to the instances of the classes
 *
 * @author Sébastien Harispe
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
