package slib.sglib.model.graph.utils;

import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import slib.sglib.model.graph.elements.E;

/**
 * WalkConstraints interface is used to define methods commonly required to
 * orient a walk according to some conditions. The aim is to provide a way to
 * distinguish edges which can be traversed from a vertex. The conditions can be
 * based on:
 * <ul>
 * <li>the admitted predicate URIs</li>
 * <li>the direction of the edges</li>
 * </ul>
 *
 * @author Sebastien Harispe
 *
 */
public interface WalkConstraint {

    /**
     * The method defines the behavior of a walk reaching the given edge. Return
     * true if the walk must continues (false if the walk must stop).
     *
     * @param e the vertex to evaluates
     * @param dir the direction of the edge
     * @return a boolean value defining if the walk must continue (true:yes,
     * false:no)
     */
    public boolean respectConstaints(E e, Direction dir);

    /**
     * Access to a view of the set of predicates (edge types) the walk consider
     * during edge evaluation. Note that during the walk the constraints are
     * defined by sets of tuples {URI, Direction}, thus in some cases the walk
     * constraint doesn't admit all edges of the type returned by this method.
     *
     * @return a view of the set of URIs corresponding to the predicates
     * accepted by the constraint
     */
    public Set<URI> getAcceptedPredicates();

    /**
     * Return the direction associated to the given predicate the object admits
     *
     * @param uri the URI of the predicate of interest
     * @return a direction IN, OUT, BOTH, null if the evaluated predicate is not
     * registered by the object
     */
    public Direction getAssociatedDirection(URI uri);

    /**
     * Add to the current constraints the ones defines in the walk constraint passed in parameter. 
     * @param wc the walk constraint
     */
    public void addWalkconstraints(WalkConstraint wc);
    
    /**
     * Add the current Traversal in the mapping of admitted traversal.
     *
     * @param pred the predicate URI (not null).
     * @param dir the accepted direction corresponding to the given predicate
     * (not null).
     */
    public void addAcceptedTraversal(URI pred, Direction dir);
    
     /**
     * Add the current Traversal in the mapping of admitted traversal.
     *
     * @param pred the predicate URI (not null).
     * @param dir the accepted direction corresponding to the given predicate
     * (not null).
     */
    public void addAcceptedTraversal(Set<URI> pred, Direction dir);

    /**
     * Access to a view of the set of predicates which can be traversed from
     * target to source, i.e. in the opposite direction. Return the set of URI
     * corresponding to the valid predicates
     *
     * @return
     */
    public Set<URI> getAcceptedWalks_DIR_IN();

    /**
     * Access to a view of the set of predicates which can be traversed from
     * source to target i.e. in the original direction.
     *
     * @return
     */
    public Set<URI> getAcceptedWalks_DIR_OUT();

    /**
     * Intersection of {@link #getAcceptedWalks_DIR_IN()} and
     * {@link #getAcceptedWalks_DIR_OUT()}. Return the set of URIs corresponding
     * to the valid predicates
     *
     * @return
     */
    public Set<URI> getAcceptedWalks_DIR_BOTH();

}
