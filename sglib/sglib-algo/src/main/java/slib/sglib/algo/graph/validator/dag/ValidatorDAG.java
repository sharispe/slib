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
package slib.sglib.algo.graph.validator.dag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.sglib.algo.graph.utils.VColor;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.type.VType;
import slib.sglib.model.graph.utils.Direction;
import slib.sglib.model.graph.utils.WalkConstraints;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.impl.SetUtils;
import sun.security.krb5.internal.crypto.EType;

/**
 * Used to validate if a graph is directed and acyclic (DAG)
 *
 * @todo use {@link WalkConstraints} to simplify the code and the parameters passed to the methods.
 * @author Sebastien Harispe
 *
 */
public class ValidatorDAG {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    G graph;
    Set<URI> edgeTypes;
    HashMap<V, VColor> verticesColors;
    Direction direction;
    boolean valid;
    E lastEdge;
    Path currentPath;

    /*---------------------------------------------------------------------*
     *  Algorithm
     *---------------------------------------------------------------------*/
    /**
     *
     * @param graph
     * @param startingURIs
     * @param edgesTypes
     * @param dir
     * @return true if the graph is a DAG
     * @throws SLIB_Ex_Critic
     */
    public boolean isDag(G graph, Set<URI> startingURIs, Set<URI> edgesTypes, Direction dir) throws SLIB_Ex_Critic {

        this.direction = dir;

        this.graph = graph;
        this.edgeTypes = edgesTypes;
        this.verticesColors = new HashMap<V, VColor>();
        valid = true;

        logger.debug("Cheking DAG property of : " + graph.getURI());
        logger.debug("starting nodes          : " + startingURIs.size());
        logger.debug("eTypes                  : " + edgesTypes);
        logger.debug("Dir                     : " + direction);

        if (startingURIs.size() < 10) {
            logger.debug("starting vertices : " + startingURIs);
        }


        if (startingURIs == null || startingURIs.isEmpty()) {
            return false;
        }

        currentPath = new Path();

        for (Value rootUri : startingURIs) {


            V root = graph.getV(rootUri);

            if (root == null) {
                throw new SLIB_Ex_Critic("Vertex '" + rootUri + "' not found in " + graph.getURI());
            }

            if (valid) {
                performDFS(root);
            }
        }

        logger.info("isDag : " + valid);
        if (!valid) {

            logger.info("current path :" + currentPath.toString());
            logger.info("Cycle detected adding : " + lastEdge + " to path");
        }
        return valid;
    }

    private void performDFS(V v) {

        if (!valid) {
            return;
        }

        if (!verticesColors.containsKey(v)) {

            verticesColors.put(v, VColor.ORANGE);
            currentPath.addVertex(v);

            Set<E> edges = graph.getE(edgeTypes, v, VType.CLASS, direction);


            for (E e : edges) {

                if (!valid) {
                    return;
                }

                V target = e.getSource();

                if (direction == Direction.OUT) {
                    target = e.getTarget();
                }

                if (verticesColors.get(target) != VColor.RED) {
                    lastEdge = e;
                    performDFS(target);
                }

            }
            if (!valid) {
                return;
            }
            currentPath.removeLastVertex();
            verticesColors.put(v, VColor.RED);

        } else if (verticesColors.get(v) == VColor.ORANGE) {
            valid = false;
        }
    }

    /*---------------------------------------------------------------------*
     *  Utils
     *---------------------------------------------------------------------*/
    /**
     * Check if a taxonomic graph or the underlying taxonomic graph of a graph
     * is a DAG. shortcut of only considering
     * SUPERCLASSOF as set of edge types
     *
     * @param graph the graph on which the evaluation has to be made
     * @return true if the the (underlying) taxonomic graph is a DAG
     *
     * @throws SLIB_Ex_Critic 
     */
    public boolean containsTaxonomicDag(G graph) throws SLIB_Ex_Critic {
        return isDag(graph, RDFS.SUBCLASSOF, Direction.IN);
    }

    /**
     * Check if the underlying graph defined by the edges of the given edge type
     * is a DAG. shortcut of {@link ValidatorDAG#isDag(G, Set)} only considering
     * the given edge type
     *
     * @param graph the graph on which the evaluation has to be made
     * @param type 
     * @param dir 
     * @return true if the the (underlying) graph reduction is a DAG
     * @throws SLIB_Ex_Critic  
     *
     */
    public boolean isDag(G graph, URI type, Direction dir) throws SLIB_Ex_Critic {
        return isDag(graph, SetUtils.buildSet(type), dir);
    }

    /**
     * Check if the underlying graph defined by the edges of the given edge
     * types and build using a traversal starting from the given root node is a
     * DAG. shortcut of {@link ValidatorDAG#isDag(G, Set, Set)} only considering
     * the given set of edge types
     *
     * @param graph the graph on which the evaluation has to be made
     * @param rootURI 
     * @param edgeTypes 
     * @param dir 
     * @return true if the the (underlying) graph reduction is a DAG
     * @throws SLIB_Ex_Critic  
     *
     */
    public boolean isDag(G graph, URI rootURI, Set<URI> edgeTypes, Direction dir) throws SLIB_Ex_Critic {
        return isDag(graph, SetUtils.buildSet(rootURI), edgeTypes, dir);
    }

    /**
     *
     * @param graph
     * @return
     * @throws SLIB_Ex_Critic
     */
    public boolean containsTaxonomicalDag(G graph) throws SLIB_Ex_Critic {
        return isDag(graph, SetUtils.buildSet(RDFS.SUBCLASSOF), Direction.IN);
    }

    /**
     * Check if the underlying graph defined by the edges of the given edges
     * type and build using a traversal starting from the given root node is a
     * DAG. shortcut of {@link ValidatorDAG#isDag(G, URI, Set)} only considering
     * the given edge type
     *
     * @param graph the graph on which the evaluation has to be made
     * @param rootURI 
     * @param type 
     * @param dir 
     * @return true if the the (underlying) graph reduction is a DAG
     *
     * @throws SLIB_Ex_Critic 
     */
    public boolean isDag(G graph, URI rootURI, URI type, Direction dir) throws SLIB_Ex_Critic {
        return isDag(graph, rootURI, SetUtils.buildSet(type), dir);
    }

    /**
     * Check if the underlying graph defined by the edges of edgeTypes, the
     * given edges type ,and build using a traversal starting from the root
     * vertices according to the inverse of edgeTypes is a DAG. shortcut of
     * {@link ValidatorDAG#isDag(G, Set, Set)} considering the given edge types
     * and the root vertices according to the inverse of the specified edge
     * types as root (see {@link ValidatorDAG#getDAGRoots(G, EType)})
     *
     * @param graph the graph on which the evaluation has to be made
     * @param edgeTypes 
     * @param dir 
     * @return true if the the (underlying) graph reduction is a DAG
     *
     * @throws SLIB_Ex_Critic 
     */
    public boolean isDag(G graph, Set<URI> edgeTypes, Direction dir) throws SLIB_Ex_Critic {

        Set<V> startingNodes = getDAGRoots(graph, edgeTypes, dir.getOpposite());
        
        logger.info("Starting process from "+startingNodes.size()+" vertices");

        if (startingNodes.isEmpty()) // No root No Dag
        {
            return false;
        }

        HashSet<URI> startingURIs = new HashSet<URI>();

        for (V v : startingNodes) {
            startingURIs.add((URI) v.getValue());
        }

        return isDag(graph, startingURIs, edgeTypes, dir);
    }

    /**
     * Root vertices (terminal vertices) are considered respect the following
     * restrictions :<br/> <ul> <li> must not contains an edges of the given
     * types and direction </li> </ul>
     *
     * Do not check if the graph is a DAG
     *
     * @param g the graph on which the root vertices need to be retrieve
     * @param etypes e.g. if taxonomic graph use SUPERCLASSOF
     * @param dir 
     * @return The set of vertices matching the predefined conditions
     */
    public Set<V> getDAGRoots(G g, Set<URI> etypes, Direction dir) {


        Set<V> roots = g.getV_NoEdgeType(VType.CLASS, etypes, dir);
        return roots;
    }

    /**
     *
     * @param g
     * @param etypes
     * @param dir
     * @return
     */
    public Set<V> getDAGRoots(G g, URI etypes, Direction dir) {
        return getDAGRoots(g, SetUtils.buildSet(etypes), dir);
    }

    /**
     * Check if the given graph contains a unique underlying rooted taxonomic
     * graph Do not check if the graph is a DAG Shortcut of
     * {@link ValidatorDAG#getDAGRoots(G, EType)} == 1 only considering
     * SUBCLASSOF relationships
     *
     * @param g the graph on which the test is performed
     * @return true if the graph contains a unique underlying rooted taxonomic
     * graph
     *
     * @throws SLIB_Ex_Critic 
     */
    public boolean containsRootedTaxonomicDag(G g) throws SLIB_Ex_Critic {


        Set<V> roots = getDAGRoots(g, SetUtils.buildSet(RDFS.SUBCLASSOF), Direction.OUT);

        logger.info("Number of roots " + roots.size());

        if (roots.size() == 1) {
            isDag(g, (URI) roots.iterator().next().getValue(), RDFS.SUBCLASSOF, Direction.IN);
        } else {
            valid = false;
        }

        logger.debug("isRootedTaxonomicDag (" + roots.size() + " root(s)) valid " + valid);

        return valid;
    }

    /**
     * Return the vertex rooting the unique underlying rooted taxonomic graph.
     *
     * Do not check if the graph is a DAG
     *
     * If the given graph does not contain a unique underlying rooted taxonomic
     * graph an SGL_Exception_Critical exception is thrown.
     *
     * Calls {@link ValidatorDAG#getDAGRoots(G, EType)} only considering
     * SUBCLASSOF relationships
     *
     * @param g the graph
     * @return the root vertex
     *
     * @throws SLIB_Ex_Critic 
     * @see
     */
    public V getRootedTaxonomicDAGRoot(G g) throws SLIB_Ex_Critic {

        Set<V> roots = getDAGRoots(g, SetUtils.buildSet(RDFS.SUBCLASSOF), Direction.OUT);

        if (roots.size() != 1) {
            throw new SLIB_Ex_Critic("Multiple root detected");
        }

        return roots.iterator().next();
    }

    /**
     * Do not check if the graph is a DAG
     *
     * @param g
     * @return
     */
    public Set<V> getTaxonomicDAGRoots(G g) {
        return getDAGRoots(g, SetUtils.buildSet(RDFS.SUBCLASSOF), Direction.OUT);
    }

    /**
     * Do not check if the graph is a DAG
     *
     * @param g
     * @param etype
     * @return
     */
    public Set<V> getDAGRoots(G g, URI etype) {
        return getDAGRoots(g, SetUtils.buildSet(etype), Direction.OUT);
    }

    /**
     * Test if the given URI corresponds to a root of the graph build according to the specified edge types.
     * @param g the graph to consider
     * @param rootURI the URI to test 
     * @param edgesType the edge type to consider only OUT direction is considered
     * @return true if the graph defined by the specified constraint is rooted by the given URI
     */
    public boolean containsRootedDagRoot(G g, URI rootURI, Set<URI> edgesType) {

        for (V v : getDAGRoots(g, edgesType, Direction.OUT)) {
            if (v.equals(rootURI)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param g
     * @param root 
     * @param edgesType
     * @return
     * @throws SLIB_Ex_Critic 
     */
    public boolean isUniqueRootedDagRoot(G g, V root, URI edgesType) throws SLIB_Ex_Critic {

        return isUniqueRootedDagRoot(g, root, SetUtils.buildSet(edgesType), Direction.IN);
    }

    /**
     * @param g
     * @param root 
     * @return
     * @throws SLIB_Ex_Critic 
     */
    public boolean isUniqueRootedTaxonomicDag(G g, V root) throws SLIB_Ex_Critic {

        return isUniqueRootedDagRoot(g, root, SetUtils.buildSet(RDFS.SUBCLASSOF), Direction.IN);
    }

    /**
     * Do not check if the graph is a DAG
     *
     * @param g
     * @param root 
     * @param edgesType
     * @param dir 
     * @return
     * @throws SLIB_Ex_Critic  
     */
    public boolean isUniqueRootedDagRoot(G g, V root, Set<URI> edgesType, Direction dir) throws SLIB_Ex_Critic {

        if (isDag(g, edgesType, dir)) {

            Set<V> roots = getDAGRoots(g, edgesType, dir.getOpposite());

            if (roots.size() == 1 && roots.iterator().next().equals(root)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param g
     * @param root
     * @param edgesType
     * @param dir
     * @return
     * @throws SLIB_Ex_Critic
     */
    public boolean isUniqueRootedDagRoot(G g, V root, URI edgesType, Direction dir) throws SLIB_Ex_Critic {

        return isUniqueRootedDagRoot(g, root, SetUtils.buildSet(edgesType), dir);
    }

    /**
     *
     * @return
     */
    public E getLastEdge() {
        return lastEdge;
    }
}
