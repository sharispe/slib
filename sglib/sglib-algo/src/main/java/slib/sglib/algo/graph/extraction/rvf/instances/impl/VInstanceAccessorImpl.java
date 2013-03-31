package slib.sglib.algo.graph.extraction.rvf.instances.impl;

import java.util.Map;
import org.openrdf.model.URI;

import slib.sglib.algo.graph.extraction.rvf.instances.VirtualInstancesAccessor;

/**
 *
 * @author seb
 */
public class VInstanceAccessorImpl implements VirtualInstancesAccessor {

    /**
     *
     */
    public Map<URI, Long> nbDirectInstances;
    /**
     *
     */
    public Map<URI, Long> nbInferredInstances;

    /**
     *
     * @param nbDirectInstances
     * @param nbInferredInstances
     */
    public VInstanceAccessorImpl(Map<URI, Long> nbDirectInstances, Map<URI, Long> nbInferredInstances) {

        this.nbDirectInstances = nbDirectInstances;
        this.nbInferredInstances = nbInferredInstances;
    }

    public long getInstancesNumber(URI v) {

        if (!nbInferredInstances.containsKey(v)) {
            return -1;
        }
        return nbInferredInstances.get(v);
    }

    public long getDirectInstancesNumber(URI v) {

        if (!nbDirectInstances.containsKey(v)) {
            return -1;
        }
        return nbDirectInstances.get(v);
    }

    public Map<URI, Long> getInferredInstancesNumberMapping() {
        return nbInferredInstances;
    }

    public Map<URI, Long> getDirectInstancesNumberMapping() {
        return nbDirectInstances;
    }
}
