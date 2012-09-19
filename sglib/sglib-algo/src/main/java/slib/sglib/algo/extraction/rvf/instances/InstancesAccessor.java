package slib.sglib.algo.extraction.rvf.instances;

import java.util.Set;

import slib.sglib.model.graph.elements.V;

public interface InstancesAccessor extends VirtualInstancesAccessor{
	
	
	public Set<V> getInstances();
	
	/**
	 * Access to the set of vertex considered as instance of a class 
	 * (using RDFS.SubClassOf inferences)
	 * @param v the class of interest 
	 * @return the set of instances of the specified class 
	 */
	public Set<V> getInstances(V v);
	
	
	/**
	 * Access to the set of vertex considered as direct instance of a class 
	 * e.g. all x respecting <x RDF.TYPE class>
	 * @param v the class of interest 
	 * @return the set of direct instances of the specified class 
	 */
	public Set<V> getDirectInstances(V v);
	
	
	public Set<V> getDirectClass(V v);
	
}
