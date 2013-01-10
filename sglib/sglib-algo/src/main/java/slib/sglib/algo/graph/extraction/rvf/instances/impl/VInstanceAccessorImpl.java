package slib.sglib.algo.graph.extraction.rvf.instances.impl;

import java.util.Map;

import slib.sglib.algo.graph.extraction.rvf.instances.VirtualInstancesAccessor;
import slib.sglib.model.graph.elements.V;

/**
 *
 * @author seb
 */
public class VInstanceAccessorImpl implements VirtualInstancesAccessor{

	
	/**
     *
     */
    public Map<V,Long> nbDirectInstances;
	/**
     *
     */
    public Map<V,Long> nbInferredInstances;
	
	
	/**
     *
     * @param nbDirectInstances
     * @param nbInferredInstances
     */
    public VInstanceAccessorImpl( Map<V,Long> nbDirectInstances,  Map<V,Long> nbInferredInstances){
		
		this.nbDirectInstances   = nbDirectInstances;
		this.nbInferredInstances = nbInferredInstances;
	}
	
	public long getInstancesNumber(V v) {
		
		if(!nbInferredInstances.containsKey(v))
			return -1;
		return nbInferredInstances.get(v);
	}

	public long getDirectInstancesNumber(V v) {
		
		if(!nbDirectInstances.containsKey(v))
			return -1;
		return nbDirectInstances.get(v);
	}

	public Map<V, Long> getInferredInstancesNumberMapping() {
		return nbInferredInstances;
	}

	public Map<V, Long> getDirectInstancesNumberMapping() {
		return nbDirectInstances;
	}
	

}
