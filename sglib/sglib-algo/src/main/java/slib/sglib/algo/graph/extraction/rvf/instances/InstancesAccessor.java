package slib.sglib.algo.graph.extraction.rvf.instances;

import java.util.Set;
import slib.sglib.model.graph.elements.V;

/**
 *
 * @author seb
 */
public interface InstancesAccessor extends VirtualInstancesAccessor {

    /**
     *
     * @return
     */
    public Set<V> getInstances();

    /**
     * Access to the set of vertex considered as instance of a class (using
     * RDFS.SubClassOf inferences)
     *
     * @param v the class of interest
     * @return the set of instances of the specified class
     */
    public Set<V> getInstances(V v);

    /**
     * Access to the set of vertex considered as direct instance of a class e.g.
     * all x respecting <x RDF.TYPE class>
     *
     * @param v the class of interest
     * @return the set of direct instances of the specified class
     */
    public Set<V> getDirectInstances(V v);

    /**
     * Method providing access to the class annotating an instance. This method
     * doesn't support inference, i.e. considering an instance y only the vertex
     * x for which y RDF.TYPE x are returned.
     *
     * @param v the vertex corresponding to the instance
     * @return the set of classes associated to the instance
     */
    public Set<V> getDirectClass(V v);
}
