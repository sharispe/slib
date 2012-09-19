package slib.sglib.algo.extraction.rvf.instances;

import java.util.Map;

import slib.sglib.model.graph.elements.V;

/**
 * @author Sebastien Harispe
 *
 */
public interface VirtualInstancesAccessor {

	/**
	 * Return the number of instances associated to a class
	 * (using RDFS.SubClassOf inferences)
	 * @param v the class of interest
	 * @return the number of instances associated to the specified class
	 */
	public long getInstancesNumber(V v);

	
	public Map<V, Long> getInferredInstancesNumberMapping();
	/**
	 * Access to the number of vertex considered as direct instance of a class 
	 * e.g. all x respecting <x RDF.TYPE class>
	 * @param v the class of interest
	 * @return the number of instances associated to the specified class
	 */
	public long getDirectInstancesNumber(V v);

	public Map<V, Long> getDirectInstancesNumberMapping();



}
