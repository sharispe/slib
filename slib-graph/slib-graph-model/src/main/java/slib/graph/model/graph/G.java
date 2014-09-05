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
package slib.graph.model.graph;

import java.util.Set;
import org.openrdf.model.URI;
import slib.graph.model.graph.elements.E;
import slib.graph.model.graph.utils.Direction;
import slib.graph.model.graph.utils.WalkConstraint;

/**
 * Interface of a multi directed Graph defined as a set of vertices and a set of
 * oriented edges. Vertices are identified by {@link URI}, edge by a triplet
 * subject predicate object (s,p,o) of URIs. The model doesn't support multiple
 * edges s,p,o of the same predicate (p), see {@link E} interface.
 *
 * The main aim is to provide an easy to use graph model to manipulate semantic
 * graphs composed of classes (concept) and instances identified by URIs. The
 * classes and instances can established semantic relationships through
 * triplets. All the vertices of the graph are uniquely identified by URIs, we
 * therefore sometimes refer to an URIs corresponding to a vertex through the
 * term vertex.
 *
 * This graph is not RDF compliant as it doesn't support values such as Literals
 * or blank nodes.
 *
 * In the documentation of this class we refer to a triplet or URIs through the
 * term edge. An edge is therefore a statement in which the subject, the
 * predicate and the object are URIs. The subject of the edge is called the
 * source, the predicate is sometimes called the type of the edge, and the
 * object is called the target.
 *
 * @see URI
 * @see E
 * @see Direction
 * @see WalkConstraint
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public interface G {


    /**
     * Access to a view of the set of edges contained in the graph.
     *
     * @return an unmodifiable view of the set of edges contained in the graph.
     */
    public Set<E> getE();

    /**
     * Access to a view of all edges involving a specific vertex considering a
     * particular direction.
     * <ul>
     * <li> Direction.OUT: all edges for which the specified vertex is the
     * source. </li>
     * <li> Direction.IN: all edges for which the specified vertex is the
     * target. </li>
     * <li> Direction.BOTH: all edges involving the specified vertex, i.e. union
     * IN and OUT. </li>
     * </ul>
     *
     * If the vertex is set to null all edges are retrieved. Setting the
     * direction to null is the same as Direction.BOTH.
     *
     * @param v the vertex of interest
     * @param dir the direction to consider i.e. IN, OUT or BOTH, see
     * {@link Direction}
     * @return the set of edges corresponding to the query. The method return an
     * empty set if no results are associated to the query.
     */
    public Set<E> getE(URI v, Direction dir);

    /**
     * Retrieve a view of all edges characterized by the specified URI
     * predicate. No restriction is applied if the given URI is equals to null.
     *
     * @param predicate the predicate URI of interest
     * @return a set of edges respecting the given constraints (empty set if no
     * results)
     */
    public Set<E> getE(URI predicate);

    /**
     * Retrieve a view of all edges characterized by one of specified predicate
     * URIs. If the given set of types (predicated )is empty or equal to null no
     * restriction is applied and all edges will be returned.
     *
     * @param types the set of predicate URIs of interest
     * @return the set of edges of the graph respecting the given constraints
     * (empty set if no results)
     */
    public Set<E> getE(Set<URI> types);

    /**
     * Retrieve a view of all edges of the graph characterized by the specified
     * constraint. The constraint can be tuned based on the following
     * parameters:
     * <ul>
     * <li>the predicate URI</li>
     * <li>the vertex of interest</li>
     * <li>the direction to consider</li>
     * </ul>
     * If a parameter is set to null, the constraint is relaxed considering this
     * parameter. As an example if the predicate URI is set to null, all edges
     * respecting the other constraints will be returned.
     *
     * @param predicate the predicate URI of the edges of interest
     * @param v the vertex of interest
     * @param dir the direction to consider
     * @return a set of edges respecting the given constraint (empty Set if no
     * results)
     */
    public Set<E> getE(URI predicate, URI v, Direction dir);

    /**
     * Retrieve a view of all edges of the graph characterized by the
     * constraint. The constraint can be tuned based on the following
     * parameters:
     * <ul>
     * <li>the set of predicate URIs</li>
     * <li>the related vertex</li>
     * <li>the direction to consider</li>
     * </ul>
     * If a parameter is set to null, the constraint is relaxed considering this
     * parameter. As an example if the predicate URI is set to null, all edges
     * respecting the other conditions will be returned
     *
     * @param predicates the set of predicate URIs to consider.
     * @param source the vertex of interest
     * @param dir the direction to consider
     * @return a set of edges respecting the given constraint (empty Set if no
     * results)
     */
    public Set<E> getE(Set<URI> predicates, URI source, Direction dir);

    /**
     * Retrieve a view of all edges of the graph which can be reached from a
     * given vertex respecting the given constraint.
     *
     * @param v the vertex of interest
     * @param wc the object defining the constraint
     * @return the set of edges which can be reached considering the constraint.
     */
    public Set<E> getE(URI v, WalkConstraint wc);

    /**
     * Retrieve view of all vertices of the graph which can be reached from a
     * given vertex respecting the given constraint.
     *
     * @param v the vertex of interest
     * @param wc the object defining the constraint
     * @return the set of vertices which can be reached considering the
     * constraint.
     */
    public Set<URI> getV(URI v, WalkConstraint wc);

    /**
     * Add an edge of the given type (URI) between the specified source and
     * target. If the given edge already exits nothing is done. If the
     * source/target of the edge is not part of the graph it will be added.
     *
     * @param src the source of the edge (not null)
     * @param type the predicate URI of the edge to create (not null)
     * @param target the target of the edge (not null)
     */
    public void addE(URI src, URI type, URI target);

    /**
     * Add the given edge to the graph
     *
     * @param e an edge
     */
    public void addE(E e);

    /**
     * Add the given set of edges of the graph
     *
     * @param e a set of edges
     */
    public void addE(Set<E> e);

    /**
     * Used to remove an Edge of the graph
     *
     * @param e the edge to remove
     */
    public void removeE(E e);

    /**
     * Used to remove all edges of a specific type (predicate).
     *
     * @param t the type (predicate) of the edges to remove
     */
    public void removeE(URI t);

    /**
     * Used to remove a set of edges of the graph
     *
     * @param e a set of edges
     */
    public void removeE(Set<E> e);

    /**
     * Add the given vertex to the graph. Nothing is done if the graph already
     * exists.
     *
     * @param v the vertex to add
     */
    public void addV(URI v);

    /**
     * Add the given set of vertices to the graph.
     *
     * @param v the set of vertices
     */
    public void addV(Set<URI> v);

    /**
     * Remove the given vertex to the graph. All related edges (in / out) will
     * also be removed.
     *
     * @param v The vertex to remove
     */
    public void removeV(URI v);

    /**
     * Remove all specified vertices. All related edges (in / out) will also be
     * removed.
     *
     * @param setV The set of vertices to remove
     */
    public void removeV(Set<URI> setV);

    /**
     * Check if the graph contains a Vertex associated to the given Value.
     *
     * @param v the value to test
     * @return a boolean : true if the vertex exists else return false
     */
    public boolean containsVertex(URI v);

    
    /**
     * Check if the graph contains the specified triplet.
     * @param s the subject of the triplet
     * @param p the predicate of the triplet
     * @param o the object
     * @return true if the triplet exists
     */
    public boolean containsEdge(URI s, URI p, URI o);
    
    /**
     * @return an unmodifiable view of the set of vertices contained in the
     * graph.
     */
    public Set<URI> getV();

    /**
     * Return the number of vertices
     *
     * @return #vertices
     */
    public int getNumberVertices();

    /**
     * @return the number of edges
     */
    public int getNumberEdges();

    /**
     * Return all neighbors vertices of a given vertex considering a particular
     * direction and URI predicate. If the given URI is null no restriction on
     * predicate URI is applied.
     *
     * @param v the focusing vertex
     * @param predicate the URI of the edges to consider
     * @param dir the direction to consider
     * @return the set of vertices associated to the given conditions
     */
    public Set<URI> getV(URI v, URI predicate, Direction dir);

    /**
     * @return the URI associated to the graph
     */
    public URI getURI();
}
