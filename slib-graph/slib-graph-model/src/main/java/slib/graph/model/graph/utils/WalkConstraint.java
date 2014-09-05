/* 
 *  Copyright or © or Copr. Ecole des Mines d'Alès (2012-2014) 
 *  
 *  This software is a computer program whose purpose is to provide 
 *  several functionalities for the processing of semantic data 
 *  sources such as ontologies or text corpora.
 *  
 *  This software is governed by the CeCILL  license under French law and
 *  abiding by the rules of distribution of free software.  You can  use, 
 *  modify and/ or redistribute the software under the terms of the CeCILL
 *  license as circulated by CEA, CNRS and INRIA at the following URL
 *  "http://www.cecill.info". 
 * 
 *  As a counterpart to the access to the source code and  rights to copy,
 *  modify and redistribute granted by the license, users are provided only
 *  with a limited warranty  and the software's author,  the holder of the
 *  economic rights,  and the successive licensors  have only  limited
 *  liability. 

 *  In this respect, the user's attention is drawn to the risks associated
 *  with loading,  using,  modifying and/or developing or reproducing the
 *  software by the user in light of its specific status of free software,
 *  that may mean  that it is complicated to manipulate,  and  that  also
 *  therefore means  that it is reserved for developers  and  experienced
 *  professionals having in-depth computer knowledge. Users are therefore
 *  encouraged to load and test the software's suitability as regards their
 *  requirements in conditions enabling the security of their systems and/or 
 *  data to be ensured and,  more generally, to use and operate it in the 
 *  same conditions as regards security. 
 * 
 *  The fact that you are presently reading this means that you have had
 *  knowledge of the CeCILL license and that you accept its terms.
 */
package slib.graph.model.graph.utils;

import java.util.Set;
import org.openrdf.model.URI;
import slib.graph.model.graph.elements.E;

/**
 * WalkConstraint interface is used to define methods commonly required to
 * orient a walk according to some conditions. The aim is to provide a way to
 * distinguish edges which can be traversed from a vertex. The conditions can be
 * based on:
 * <ul>
 * <li>the admitted predicate URIs</li>
 * <li>the direction of the edges</li>
 * </ul>
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
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
     * Add to the current constraints the ones defines in the walk constraint
     * passed in parameter.
     *
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
     * @return the set of URIs which are accepted considering the direction IN
     */
    public Set<URI> getAcceptedWalks_DIR_IN();

    /**
     * Access to a view of the set of predicates which can be traversed from
     * source to target i.e. in the original direction.
     *
     * @return the set of URIs which are accepted considering the direction OUT
     */
    public Set<URI> getAcceptedWalks_DIR_OUT();

    /**
     * Intersection of {@link #getAcceptedWalks_DIR_IN()} and
     * {@link #getAcceptedWalks_DIR_OUT()}. Return the set of URIs corresponding
     * to the valid predicates
     *
     * @return the set of URIs which are accepted considering the directions IN and OUT
     */
    public Set<URI> getAcceptedWalks_DIR_BOTH();
    
    /**
     * Check if the walk give access to OUT edges
     * @return true if the constraints accept walks to OUT edges
     */
    public boolean acceptOutWalks();
    
    /**
     * Check if the walk give access to IN edges
     * @return true if the constraints accept walks to IN edges
     */
    public boolean acceptInWalks();
}
