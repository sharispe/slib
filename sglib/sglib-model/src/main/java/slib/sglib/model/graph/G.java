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
import org.openrdf.model.Value;
import org.openrdf.sail.NotifyingSail;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.type.VType;
import slib.sglib.model.graph.utils.Direction;
import slib.sglib.model.graph.utils.WalkConstraints;
import slib.sglib.model.graph.weight.GWS;
import slib.sglib.model.repo.DataFactory;

/**
 * Generic interface of a multi directed Graph defined as a set of vertices
 * {@link V}, a set of oriented edges {@link E}. A default weighting scheme
 * {@link GWS} is also associated to the graph in order to define default edge
 * weights.
 *
 * @author Sebastien Harispe
 *
 * @see Storage
 * @see V
 * @see E
 */
public interface G extends NotifyingSail {

    /**
     * Access to the singleton {@link DataRepository} used to create the
     * information associated to the basic element of the graph e.g. URIs
     *
     * @return the {@link DataRepository} used by the graph
     */
    public DataFactory getDataFactory();

    /**
     * Retrieve all edges contained in the graph
     *
     * @return a Set of edges (empty Set if no results)
     */
    public Set<E> getE();

    /**
     * Retrieve all edges involving a specific vertex considering a particular
     * direction  {@link Direction}
	 * {@link Direction#OUT} all edges for which the specified vertex is the
     * source {@link Direction#IN} all edges for which the specified vertex is
     * the target {@link Direction#BOTH} all edges involving the specified
     * vertex as source or target
     *
     * @param v the vertex of interest
     * @return a Set of edges corresponding to the query (empty Set if no
     * results)
     */
    public Set<E> getE(V v, Direction dir);

    /**
     * Retrieve all edges of the graph characterized by the specified URI e.g.
     * RDFS.SubClassOf If the given {@link URI} is null restriction is applied
     * and all edges are returned.
     *
     * @param t the URI of interest
     * @return a Set of edges of the specified URI (empty Set if no results)
     */
    public Set<E> getE(URI t);

    /**
     * Retrieve all edges of the graph characterized by the specified URIs e.g.
     * RDFS.SubClassOf, RDF.Type If the given set of {@link URI} is null all
     * edges are returned.
     *
     * @param c the set of URIs of interest
     * @return a Set of edges of the specified URI (empty Set if no results)
     */
    public Set<E> getE(Set<URI> c);

    /**
     * Retrieve all edges of the graph characterized by the specified URI e.g.
     * RDFS.SubClassOf linking the given vertex as source (Direction.OUT),
     * target (Direction.IN) or both (Direction.BOTH)
     *
     * If the given {@link URI} is null no restriction on edge type is applied.
     *
     * @param t the URI of the edges of interest
     * @param v the vertex of interest
     * @param dir the {@link Direction} to consider
     * @return a Set of edges respecting the given query (empty Set if no
     * results)
     */
    public Set<E> getE(URI t, V v, Direction dir);

    /**
     * Retrieve all edges of the graph characterized by the specified URI e.g.
     * RDFS.SubClassOf linking the given vertices as source (Direction.OUT),
     * target (Direction.IN) or both (Direction.BOTH)
     *
     * If the given {@link URI} is null no restriction on edge type is applied.
     *
     * @param t the URI of the edges of interest
     * @param vertices the set of vertices of interest
     * @param dir the {@link Direction} to consider
     * @return a Set of edges respecting the given query (empty Set if no
     * results)
     */
    public Set<E> getE(URI t, Set<V> vertices, Direction dir);

    /**
     * Retrieve all edges of the graph characterized by the specified URIs e.g.
     * RDFS.SubClassOf linking the given vertices as source (Direction.OUT),
     * target (Direction.IN) or both (Direction.BOTH)
     *
     * If the given set of {@link URI} is null no restriction on edge type is
     * applied.
     *
     * @param t the URI of the edges of interest
     * @param v the vertex of interest
     * @param dir the {@link Direction} to consider
     * @return a Set of edges respecting the given query (empty Set if no
     * results)
     */
    public Set<E> getE(Set<URI> t, V source, Direction dir);

    /**
     * Retrieve all edges of the graph characterized by the specified URI e.g.
     * RDFS.SubClassOf linking the given vertex as source (Direction.OUT),
     * target (Direction.IN) or both (Direction.BOTH) to a vertex of the given
     * {@link VType}
     *
     * If the given {@link VType} is null no restriction on {@link VType} is
     * applied. If the given {@link URI} is null no restriction on edge type is
     * applied.
     *
     * @param t the URI of the edges of interest
     * @param v the vertex of interest
     * @param type the type of vertex the edges must link v to
     * @param dir the {@link Direction} to consider
     * @return a Set of edges respecting the given query (empty Set if no
     * results)
     */
    public Set<E> getE(URI t, V v, VType type, Direction dir);

    /**
     * Retrieve all edges of the graph characterized by the specified URIs e.g.
     * RDFS.SubClassOf linking the given vertex as source (Direction.OUT),
     * target (Direction.IN) or both (Direction.BOTH) to a vertex of the given
     * {@link VType}
     *
     * If the given {@link VType} is null no restriction on {@link VType} is
     * applied. If the given set of {@link URI} is null no restriction on edge
     * type is applied.
     *
     * @param pTypes the set of URIs of the edges of interest
     * @param v the vertex of interest
     * @param type the type of vertex the edges must link v to
     * @param dir the {@link Direction} to consider
     * @return a Set of edges respecting the given query (empty Set if no
     * results)
     */
    public Set<E> getE(Set<URI> pTypes, V v, VType type, Direction dir);

    /**
     * Retrieve all edges of the graph characterized by the specified URIs e.g.
     * RDFS.SubClassOf linking the given vertex as source (Direction.OUT),
     * target (Direction.IN) or both (Direction.BOTH) to a vertex of the given
     * {@link VType}s If the given set of {@link VType} is null no restriction
     * on {@link VType} is applied. If the given set of {@link URI} is null no
     * restriction on edge type is applied.
     *
     * @param pTypes the set of URIs of the edges of interest
     * @param v the vertex of interest
     * @param vTypes the types of vertex the selected can linked v to
     * @param dir the {@link Direction} to consider
     * @return a Set of edges respecting the given query (empty Set if no
     * results)
     */
    public Set<E> getE(Set<URI> pTypes, V v, Set<VType> vTypes, Direction dir);

    public Set<E> getE(V v, WalkConstraints wc);

    public Set<V> getV(V v, WalkConstraints wc);

    /**
     * Add an edge of the given type (URI) between the specified source and
     * target If the given edge already exits nothing is done. If the involved
     * don't exist they are created.
     *
     * @param src the source of the edge
     * @param target the target of the edge
     * @param type the {@link URI} of the edge to create
     */
    public void addE(V src, V target, URI type);

    /**
     * Add the given edge to the graph
     *
     * @param e an edge
     */
    public void addE(E e);

    /**
     * Add the given set of edges of the graph
     *
     * @param a set of edges
     */
    public void addEdges(Set<E> e);

    /**
     * Used to remove an Edge of the graph
     *
     * @param the edge to remove
     */
    public void removeE(E e);

    /**
     * Used to remove all Edge of a specific EdgeType If the given URI is null
     * only edges with URI==null are returned
     *
     * @param the EdgeType of the edges to remove
     */
    public void removeE(URI t);

    /**
     * Used to remove a set of edges of the graph
     *
     * @param a set of edges
     */
    public void removeE(Set<E> e);

    /**
     * Add the given vertex to the Graph
     *
     * @param the Vertex to add
     * @return the object corresponding to the given vertex in the graph. If a
     * similar vertex exists (
     * @see {@link V#equals(Object)})the already loaded vertex is returned.
     */
    public V addV(V v);

    /**
     * Add the given set of vertices to the Graph
     *
     * @param v the set of vertices
     * @see G#addV(V)
     */
    public void addV(Set<V> v);

    /**
     * Remove the given vertex to the graph. All related edges (in / out) are
     * also removed
     *
     * @param The Vertex to remove
     */
    public void removeV(V v);

    /**
     * Remove all specified Vertices and related edges (in / out)
     *
     * @param The Set of vertices to remove
     * @see G#removeV(V);
     */
    public void removeV(Set<V> setV);

    /**
     * Check if the graph contains at least one edge linking the v1 to v2
     * respecting the given direction
     *
     * @param v1 the first vertex
     * @param v2 the second vertex
     * @param dir the {@link Direction} to consider
     * @return a boolean : true if edge exists else return false
     */
    public boolean containsEdge(V v1, V v2, Direction dir);

    /**
     * Check if the graph contains at least one edge of the given URI linking
     * the v1 to v2 respecting the given direction If the given URI is null no
     * restriction on edge type is applied.
     *
     * @param v1 the first vertex
     * @param v2 the second vertex
     * @param dir the {@link Direction} to consider
     * @param type the URI of the edges to consider
     * @return a boolean : true if edge exists else return false
     */
    public boolean containsEdge(V v1, V v2, Direction dir, URI type);

    /**
     * Check if the graph contains at least one edge characterized by the given
     * URI If the given URI is null no restriction on edge type is applied.
     *
     * @param t the URI of the edge
     * @return a boolean : true if an edge exists else return false
     */
    public boolean containsEdgeOfType(URI t);

    /**
     * Check if the graph contains the specified Vertex
     *
     * @param v the Vertex to test
     * @return a boolean : true if the vertex exists else return false
     * @see V#equals(Object)
     */
    public boolean containsVertex(V v);

    /**
     * Check if the graph contains a Vertex associated to the given
     * {@link Value}
     *
     * @param the value to test
     * @return a boolean : true if the vertex exists else return false
     * @see V#equals(Object)
     */
    public boolean containsVertex(Value v);

    /**
     * Access to the vertex associated to the given {@link Value}
     *
     * @param value the {@link Value} to consider
     * @return a vertex or null
     */
    public V getV(Value value);

    /**
     * @return A set of Vertex containing all vertices of the graph
     */
    public Set<V> getV();

    /**
     * Return graph vertices respecting the given type {@link VType} If the
     * given {@link VType} is null no restriction on {@link VType} is applied
     * i.e. similar call to {@link #getV()}
     *
     * @param type
     * @return the corresponding set of vertices
     */
    public Set<V> getV(VType type);

    /**
     * Return graph vertices respecting one of the given type If the given set
     * of {@link VType} is null no restriction on {@link VType} is applied.
     *
     * @param Set<VType> types
     * @return the corresponding set of vertices
     */
    public Set<V> getV(Set<VType> type);

    /**
     * Return the set of vertices contained in the underlying taxonomic graph
     * Shortcut of {@link G#getV(VType)} with {@link VType} equals to
     * {@link VTypeGeneric#CLASS}
     *
     * @return The set of {@link VClass} contained in the graph i.e. the
     * vertices composing the underlying taxonomic graph
     */
    public Set<V> getVClass();

    /**
     * Return the number of vertices
     *
     * @return #vertices
     */
    public long getNumberVertices();

    /**
     * Return the number of vertices of type {@link VTypeGeneric#CLASS}
     *
     * @return number of vertices composing the underlying taxonomic graph
     */
    public long getNumberVClass();

    /**
     * Access to the number of edges
     *
     * @return the number of edges
     */
    public long getNumberEdges();

    /**
     * Access to the default weight of edges characterized by the given
     * {@link URI}
     *
     * @param uri
     * @return the default weight of the edges associated to the given
     * {@link URI}
     */
    public double getEdgeTypeWeight(URI uri);

    /**
     * Set the default weight of the edges associated to the given {@link URI}
     *
     * @param uri the {@link URI} of the edges to consider
     * @param w the weight
     * @param propagate if false only the default value is set, if true all
     * weights associated to the edges characterized by the given URI will be
     * set.
     */
    public void setEdgeTypeWeight(URI uri, double w, boolean propagate);

    /**
     * Access to the weight to the edges
     *
     * @param e the edge of interest
     * @return the weight
     */
    public double getEdgeWeight(E e);

    /**
     * Setter of the weight of an edge
     *
     * @param e the edge
     * @param w the new weight
     */
    public void setEdgeWeight(E e, double w);

    /**
     * Access to the Graph Weighting Scheme associated to the graph
     *
     * @return the Graph Weighting Scheme
     */
    public GWS getWeightingScheme();

    /**
     * Setter of the Graph Weighting Scheme associated to the graph
     *
     * @param ws a new Graph Weighting Scheme {@link GWS}
     */
    public void setWeightingScheme(GWS ws);

    /**
     * Retrieve all vertices without in/out/both edges of the given {@link URI}
     * associated to them. If the given URI is null no restriction on edge type
     * is applied i.e. only vertices without edges considering the given
     * direction will be returned
     *
     * @param edgeType the type of edges to consider
     * @param dir the direction to consider
     * @return the set of vertices respecting the defined conditions
     */
    public Set<V> getV_NoEdgeType(URI edgeType, Direction dir);

    /**
     * Retrieve all vertices without in/out/both edges of the given {@link URI}s
     * associated to them. If the given set of URIs is null no restriction on
     * edge type is applied i.e. only vertices without edges considering the
     * given direction will be considered for further evaluation
     *
     * @param edgeTypes the types of edges to consider
     * @param dir the direction to consider
     * @return the set of vertices respecting the defined conditions
     */
    public Set<V> getV_NoEdgeType(Set<URI> edgeTypes, Direction dir);

    /**
     * Retrieve all vertices of a particular type without in/out/both edges of
     * the given {@link URI}s associated to them. If the given set of URIs is
     * null no restriction on edge type is applied i.e. only vertices without
     * edges considering the given direction will be considered for further
     * evaluation If the given {@link VType} is null no restriction on
     * {@link VType} is applied.
     *
     * @param type the types of vertices to consider
     * @param edgeTypes the types of edges to consider
     * @param dir the direction to consider
     * @return the set of vertices respecting the defined conditions
     */
    public Set<V> getV_NoEdgeType(VType type, Set<URI> edgeTypes, Direction dir);

    /**
     * Return all neighbors vertices of a given vertex considering particular
     * type or edges and a particular direction If the given set of URI is null
     * no restriction on edge type is applied.
     *
     * @param v the focusing vertex
     * @param eTypes the type of edges to consider
     * @param dir the direction to consider
     * @return the set of vertices associated to the given conditions
     */
    public Set<V> getV(V v, Set<URI> eTypes, Direction dir);

    /**
     * Return all neighbors vertices of a given vertex considering particular
     * type or edges and a particular direction If the given URI is null no
     * restriction on edge type is applied.
     *
     * @param v the focusing vertex
     * @param eType the type of edges to consider
     * @param dir the direction to consider
     * @return the set of vertices associated to the given conditions
     */
    public Set<V> getV(V v, URI eType, Direction dir);

    public URI getURI();

    public V createVertex(Value val);

    public V createVertex(Value val, VType type);
}
