package slib.sglib.algo.graph.extraction.rvf.instances;

import java.util.Map;
import org.openrdf.model.URI;

/**
 * @author Sebastien Harispe
 *
 */
public interface VirtualInstancesAccessor {

    /**
     * Return the number of instances associated to a class (using
     * RDFS.SubClassOf inferences)
     *
     * @param v the class of interest
     * @return the number of instances associated to the specified class
     */
    public long getInstancesNumber(URI v);

    /**
     *
     * @return
     */
    public Map<URI, Long> getInferredInstancesNumberMapping();

    /**
     * Access to the number of vertex considered as direct instance of a class
     * e.g. all x respecting <x RDF.TYPE class>
     *
     * @param v the class of interest
     * @return the number of instances associated to the specified class
     */
    public long getDirectInstancesNumber(URI v);

    /**
     *
     * @return
     */
    public Map<URI, Long> getDirectInstancesNumberMapping();
}
