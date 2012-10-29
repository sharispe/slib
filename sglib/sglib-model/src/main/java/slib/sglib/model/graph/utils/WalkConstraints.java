package slib.sglib.model.graph.utils;

import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.type.VType;


/**
 * WalkConstraints interface is used to define methods commonly required to 
 * orient a walk according to some conditions e.g. type of edges admitted (URI) and 
 * the directions which can be used for their traversal, the type of vertices the 
 * walk can traverse, etc.
 * 
 * @author Sebastien Harispe
 *
 */
public interface WalkConstraints {
	
	/**
	 * The method defines the behavior of a walk reaching the given vertex.
	 * Return true if the walk must continues or false if the walk must stop.
	 * @param v the vertex to evaluates
	 * @return a boolean value defining if the walk must continue (true:yes, false:no)
	 */
	public boolean respectConstaints(V v);
	
	/**
	 * The method defines the behavior of a walk reaching the given edge.
	 * Return true if the walk must continues (false if the walk must stop).
	 * @param v the vertex to evaluates
	 * @param dir the direction of the edge
	 * @return a boolean value defining if the walk must continue (true:yes, false:no)
	 */
	public boolean respectConstaints(E e, Direction dir);
	
	/**
	 * Access to set of predicate (edge types) the walk consider during edge evaluation
	 * @return the set of URI corresponding to the edge evaluated or empty set
	 */
	public Set<URI> getAcceptedPredicates();
	
	/**
	 * Return the direction associated to the given predicate the object admits 
	 * @param uri the URI of the predicate of interest
	 * @return a direction IN, OUT, BOTH, null if the evaluated predicate
	 * is not registered by the object
	 */
	public Direction getAssociatedDirection(URI uri);

	/**
	 * Add the current type of vertex as admitted
	 */
	public void addAcceptedVType(VType type);
	
	
	/**
	 * Return the set of VTypes the object admits 
	 * @param uri the URI of the predicate of interest
	 * @return set of VTypes admitted or empty set
	 */
	public Set<VType> getAcceptedVTypes();
	
	/**
	 * Add the current Traversal in the mapping of admitted traversal
	 */
	public void addAcceptedTraversal(URI pred, Direction dir);
	
	/**
	 * Access to the Mapping predicate/direction the object accepts
	 * @return a the traversal mapping defined or an empty map
	 */
	public Map<URI,Direction> getAcceptedTraversals();
	
	
	/**
	 * Access to the predicate which can be traversed from target <- source 
	 * i.e. in the opposite direction
	 * Return the set of URI corresponding to the valid predicates
	 */
	public Set<URI> getAcceptedWalks_DIR_IN();
	
	/**
	 * Access to the predicate which can be traversed from source -> target
	 * i.e. in the original direction
	 * Return the set of URI corresponding to the valid predicates
	 */
	public Set<URI> getAcceptedWalks_DIR_OUT();
	
	/**
	 * Intersection of {@link #getAcceptedWalks_DIR_IN()} and {@link #getAcceptedWalks_DIR_OUT()}
	 * Return the set of URI corresponding to the valid predicates
	 */
	public Set<URI> getAcceptedWalks_DIR_BOTH();
	
	/**
	 * Build a {@link WalkConstraints} object corresponding to the inverse of the current object.
	 * Definition of an inverse follows:
	 * 	- Accepted VType are unchanged
	 * 	- Accepted Predicate are unchanged
	 * 	- Direction associated to accepted predicate are the inverse of those defined:
	 * 		IN  -> OUT
	 * 		OUT -> IN
	 * 		BOTH -> BOTH (if boolean includeBOTHpredicate is set to true)
	 * 
	 * @param includeBOTHpredicate defines if the predicated associated to a direction {@link Direction#BOTH}
	 * must also be included
	 * @return a {@link WalkConstraints} object corresponding to the inverse of this one, see definition
	 * below  
	 */
	public WalkConstraints getInverse(boolean includeBOTHpredicate);
}
