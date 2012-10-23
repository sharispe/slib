package slib.sglib.algo.extraction.rvf.instances.impl;

import java.util.Map;
import java.util.Set;

import org.openrdf.model.vocabulary.RDF;

import slib.sglib.algo.extraction.rvf.instances.InstancesAccessor;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.utils.Direction;


public class InstanceAccessor_RDF_TYPE implements InstancesAccessor{

	G graph;
	
	public InstanceAccessor_RDF_TYPE(G graph){
		this.graph = graph;
	}
	
	public Set<V> getDirectInstances(V v) {
		return graph.getV(v, RDF.TYPE, Direction.IN);
	}
	
	
	public long getInstancesNumber(V v) {
		// TODO Auto-generated method stub Aug 30, 2012
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public Map<V, Long> getInferredInstancesNumberMapping() {
		// TODO Auto-generated method stub Aug 30, 2012
		throw new UnsupportedOperationException("Not supported yet.");
		
	}

	public long getDirectInstancesNumber(V v) {
		// TODO Auto-generated method stub Aug 30, 2012
		throw new UnsupportedOperationException("Not supported yet.");
		
	}

	public Map<V, Long> getDirectInstancesNumberMapping() {
		// TODO Auto-generated method stub Aug 30, 2012
		throw new UnsupportedOperationException("Not supported yet.");
		
	}

	public Set<V> getInstances() {
		// TODO Auto-generated method stub Aug 30, 2012
		throw new UnsupportedOperationException("Not supported yet.");
		
	}

	public Set<V> getInstances(V v) {
		// TODO Auto-generated method stub Aug 30, 2012
		throw new UnsupportedOperationException("Not supported yet.");
		
	}



	public Set<V> getDirectClass(V v) {
		return graph.getV(v, RDF.TYPE, Direction.OUT);
	}

}
