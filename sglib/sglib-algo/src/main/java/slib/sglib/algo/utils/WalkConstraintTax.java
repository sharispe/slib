package slib.sglib.algo.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.URI;

import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.type.VType;
import slib.sglib.model.graph.utils.Direction;
import slib.sglib.model.graph.utils.WalkConstraints;


public class WalkConstraintTax implements WalkConstraints{
	
	Map<URI,Direction> acceptedWalksIN  = new HashMap<URI, Direction>();
	Map<URI,Direction> acceptedWalksOUT = new HashMap<URI, Direction>();
	Set<URI> validPredicates = new HashSet<URI>();
	Set<VType> validVTypes   = new HashSet<VType>();
	
	public WalkConstraintTax(Map<URI,Direction> walkRules){
		for(Entry<URI, Direction> e : walkRules.entrySet()){
			
			Direction dir = e.getValue();
			
			if(dir == Direction.IN || dir == Direction.BOTH)
				acceptedWalksIN.put(e.getKey(), dir);
			
			if(dir == Direction.OUT || dir == Direction.BOTH)
				acceptedWalksOUT.put(e.getKey(), dir);
		}
		
		validPredicates.addAll(acceptedWalksIN.keySet());
		validPredicates.addAll(acceptedWalksOUT.keySet());
		
		validVTypes.add(VType.CLASS);
	}
	
	public WalkConstraintTax(URI acceptedPredicate, Direction dir){
		if(dir == Direction.IN || dir == Direction.BOTH)
			acceptedWalksIN.put(acceptedPredicate, dir);
		
		if(dir == Direction.OUT || dir == Direction.BOTH)
			acceptedWalksOUT.put(acceptedPredicate, dir);
		
		validPredicates.add(acceptedPredicate);
		validVTypes.add(VType.CLASS);
	}
	
	public boolean respectConstaints(E e, Direction dir) {
		boolean valid = false;
		
		if(dir == Direction.IN || dir == Direction.BOTH)
			valid = acceptedWalksIN.keySet().contains(e.getURI());
			
		if(dir == Direction.OUT || (!valid && dir == Direction.BOTH))
			valid = acceptedWalksOUT.keySet().contains(e.getURI());
		
		return valid;
	}
	
	public boolean respectConstaints(V v) {
		return validVTypes.contains(v.getType());
	}
	
	public Set<VType> getAcceptedVTypes() {
		return validVTypes;
	}
	
	public Set<URI> getAcceptedPredicates() {
		return validPredicates;
	}
	
	public Map<URI,Direction> getAcceptedTraversals() {
		
		Map<URI,Direction> acceptedWalks = new HashMap<URI, Direction>();
		
		acceptedWalks.putAll(acceptedWalksIN);
		acceptedWalks.putAll(acceptedWalksOUT);
		
		return acceptedWalks;
	}

	public Set<URI> getAcceptedWalks_DIR_IN() {
		return acceptedWalksIN.keySet();
	}

	public Set<URI> getAcceptedWalks_DIR_OUT() {
		return acceptedWalksOUT.keySet();
	}

	public Set<URI> getAcceptedWalks_DIR_BOTH() {
		
		Set<URI> acceptedWalks = new HashSet<URI>();
		acceptedWalks.addAll(acceptedWalksIN.keySet());
		acceptedWalks.addAll(acceptedWalksOUT.keySet());
		
		return acceptedWalks;
	}

	public WalkConstraints getInverse(boolean includeBOTHpredicates) {
		
		Map<URI,Direction> oppositeAcceptedWalks = new HashMap<URI, Direction>();
		
		for(Entry<URI, Direction> e : acceptedWalksIN.entrySet())
			oppositeAcceptedWalks.put(e.getKey(), Direction.OUT);
		
		for(Entry<URI, Direction> e : acceptedWalksOUT.entrySet())
			oppositeAcceptedWalks.put(e.getKey(), Direction.IN);
		
		return new WalkConstraintTax(oppositeAcceptedWalks);
	}

	public Direction getAssociatedDirection(URI uri) {
		Direction dir = acceptedWalksIN.get(uri);
		
		if(dir == null && acceptedWalksOUT.containsKey(uri))
			dir =  Direction.OUT;
		
		else if(dir!= null && acceptedWalksOUT.containsKey(uri)){
			dir =  Direction.BOTH;
		}
		return dir;
	}

	public void addAcceptedVType(VType type) {
		// TODO Auto-generated method stub Aug 29, 2012
		throw new UnsupportedOperationException("Not supported yet.");
		
	}

	public void addAcceptedTraversal(URI pred, Direction dir) {
		// TODO Auto-generated method stub Aug 29, 2012
		throw new UnsupportedOperationException("Not supported yet.");
		
	}
	

}
