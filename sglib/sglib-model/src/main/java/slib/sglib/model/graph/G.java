/*

 Copyright or © or Copr. Ecole des Mines d'Alès (2012) 

 This software is a computer program whose purpose is to 
 process semantic graphs.

 This software is governed by the CeCILL  license under French law and
 abiding by the rules of distribution of free software.  You can  use, 
 modify and/ or redistribute the software under the terms of the CeCILL
 license as circulated by CEA, CNRS and INRIA at the following URL
 "http://www.cecill.info". 

 As a counterpart to the access to the source code and  rights to copy,
 modify and redistribute granted by the license, users are provided only
 with a limited warranty  and the software's author,  the holder of the
 economic rights,  and the successive licensors  have only  limited
 liability. 

 In this respect, the user's attention is drawn to the risks associated
 with loading,  using,  modifying and/or developing or reproducing the
 software by the user in light of its specific status of free software,
 that may mean  that it is complicated to manipulate,  and  that  also
 therefore means  that it is reserved for developers  and  experienced
 professionals having in-depth computer knowledge. Users are therefore
 encouraged to load and test the software's suitability as regards their
 requirements in conditions enabling the security of their systems and/or 
 data to be ensured and,  more generally, to use and operate it in the 
 same conditions as regards security. 

 The fact that you are presently reading this means that you have had
 knowledge of the CeCILL license and that you accept its terms.

 */
package slib.sglib.model.graph;

import java.util.Set;
import org.openrdf.model.URI;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.utils.Direction;
import slib.sglib.model.graph.utils.WalkConstraints;
import slib.sglib.model.repo.URIFactory;

/**
 * Generic interface of a multi directed Graph defined as a set of vertices and a set of oriented edges.
 * 
 * @author Sebastien Harispe
 */
public interface G {

    /**
     * Access to the DataFactory used to create the basic element of the graph e.g. URIs
     *
     * @return the factory used to create the URIs
     */
    public URIFactory getURIFactory();

    /**
     * @return a copy of the set of edges contained in the graph
     */
    public Set<E> getE();

    /**
     * Retrieve all edges involving a specific vertex considering a particular direction.
     * <ul>
     *  <li> Direction.OUT: all edges for which the specified vertex is the source. </li>
     *  <li> Direction.IN: all edges for which the specified vertex is the target.  </li>
     *  <li> Direction.BOTH: all edges involving the specified vertex, i.e. union IN and OUT. </li>
     * </ul>
     * @param v the vertex of interest
     * @param dir 
     * @return the set of edges corresponding to the query.
     * The method return an empty set if no results are associated to the query. 
     */
    public Set<E> getE(URI v, Direction dir);

    /**
     * Retrieve all edges characterized by the specified URI predicate.
     * No restriction is applied if the  given URI is equals to null.
     *
     * @param predicate the predicate URI of interest
     * @return a set of edges respecting the given constraints (empty set if no results)
     */
    public Set<E> getE(URI predicate);

    /**
     * Retrieve all edges characterized by one of specified predicate URIs.
     * If the given set of is equals to null no restriction is applied
     *
     * @param c the set of predicate URIs of interest
     * @return a set of edges respecting the given constraints (empty set if no results)
     */
    public Set<E> getE(Set<URI> c);

    /**
     * Retrieve all edges of the graph characterized by the constraint.
     * The constraint can be tuned based on the following parameters:
     * <ul>
     *  <li>the predicate URI</li>
     *  <li>the related vertex</li>
     *  <li>the direction to consider</li>
     * </ul>
     * If a parameter is set to null, the constraint is relaxed considering this parameter.
     * As an example if the predicate URI is set to null, all edges respecting the other conditions will be returned
     * 
     * @param t the predicate URI of the edges of interest
     * @param v the vertex of interest
     * @param dir the direction to consider
     * @return a set of edges respecting the given constraint (empty Set if no results)
     */
    public Set<E> getE(URI t, URI v, Direction dir);


    /**
     * Retrieve all edges of the graph characterized by the constraint.
     * The constraint can be tuned based on the following parameters:
     * <ul>
     *  <li>the set of predicate URIs</li>
     *  <li>the related vertex</li>
     *  <li>the direction to consider</li>
     * </ul>
     * If a parameter is set to null, the constraint is relaxed considering this parameter.
     * As an example if the predicate URI is set to null, all edges respecting the other conditions will be returned
     * 
     * @param t the set of predicate URIs to consider.
     * @param source the vertex of interest
     * @param dir the direction to consider
     * @return a set of edges respecting the given constraint (empty Set if no results)
     */
    public Set<E> getE(Set<URI> t, URI source, Direction dir);

    
    /**
     *
     * @param v
     * @param wc
     * @return
     */
    public Set<E> getE(URI v, WalkConstraints wc);

    /**
     *
     * @param v
     * @param wc
     * @return
     */
    public Set<URI> getV(URI v, WalkConstraints wc);

    /**
     * Add an edge of the given type (URI) between the specified source and target.
     * If the given edge already exits nothing is done. 
     *
     * @param src the source of the edge
     * @param type the predicate URI of the edge to create
     * @param target the target of the edge
     */
    public void addE(URI src, URI type, URI target);
    
    
    /**
     * Add the given edge to the graph
     * @param e an edge
     */
    public void addE(E e);

    /**
     * Add the given set of edges of the graph
     *
     * @param e a set of edges
     */
    public void addEdges(Set<E> e);

    /**
     * Used to remove an Edge of the graph
     *
     * @param e the edge to remove
     */
    public void removeE(E e);

    /**
     * Used to remove all Edge of a specific predicate.
     *
     * @param t the EdgeType of the edges to remove
     */
    public void removeE(URI t);

    /**
     * Used to remove a set of edges of the graph
     *
     * @param e a set of edges
     */
    public void removeE(Set<E> e);

    /**
     * Add the given vertex to the Graph
     *
     * @param v the Vertex to add
     * @return the vertex corresponding to the added/corresponding vertex
     */
    public void addV(URI v);

    /**
     * Add the given set of vertices to the Graph
     *
     * @param v the set of vertices
     */
    public void addV(Set<URI> v);

    /**
     * Remove the given vertex to the graph. 
     * All related edges (in / out) are also removed
     *
     * @param v The Vertex to remove
     */
    public void removeV(URI v);

    /**
     * Remove all specified Vertices and related edges (in / out)
     *
     * @param setV The Set of vertices to remove
     */
    public void removeV(Set<URI> setV);

    /**
     * Check if the graph contains at least one edge linking vertices v1 and v2
     * (respecting the given direction).
     *
     * @param v1 the first vertex
     * @param v2 the second vertex
     * @param dir the direction to consider
     * @return a boolean : true if edge exists else return false
     */
    public boolean containsEdge(URI v1, URI v2, Direction dir);

    /**
     * Check if the graph contains an edge respecting the given constraint.
     
     * @param v1 the first vertex
     * @param v2 the second vertex
     * @param dir the direction to consider
     * @param predicate the URI of the edges to consider
     * @return a boolean : true if edge exists else return false
     */
    public boolean containsEdge(URI v1, URI v2, Direction dir, URI predicate);

    /**
     * Check if the graph contains at least one edge characterized by the given predicate URI. 
     * If the given URI is null no restriction is applied.
     *
     * @param t the URI of the edge
     * @return a boolean : true if an edge exists else return false
     */
    public boolean containsEdgeOfType(URI t);

    /**
     * Check if the graph contains a Vertex associated to the given Value.
     *
     * @param v the value to test
     * @return a boolean : true if the vertex exists else return false
     * @see V#equals(Object)
     */
    public boolean containsVertex(URI v);

    
    /**
     * @return A copy of the set of vertices contained in the graph.
     */
    public Set<URI> getV();


    /**
     * Return the number of vertices
     *
     * @return #vertices
     */
    public long getNumberVertices();

  
    /**
     * @return the number of edges
     */
    public long getNumberEdges();

    /**
     * Return all neighbors vertices of a given vertex considering particular predicate URIs and direction.
     * If the given set of URI is null no restriction on edge type is applied.
     *
     * @param v the focusing vertex
     * @param eTypes the type of edges to consider
     * @param dir the direction to consider
     * @return the set of vertices associated to the given conditions
     */
    public Set<URI> getV(URI v, Set<URI> eTypes, Direction dir);

    /**
     * Return all neighbors vertices of a given vertex considering a particular
     * direction and URI predicate.
     * If the given URI is null no restriction on predicate URI is applied.
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
