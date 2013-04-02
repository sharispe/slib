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
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.sglib.algo.graph.accessor.GraphAccessor;
import slib.sglib.algo.graph.utils.Color;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.utils.Direction;
import slib.sglib.model.graph.utils.WalkConstraint;
import slib.sglib.utils.WalkConstraintGeneric;
import slib.sglib.utils.WalkConstraintUtils;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.impl.SetUtils;

/**
 * Used to validate if a graph is directed and acyclic (DAG)
 *
 * @todo use {@link WalkConstraints} to simplify the code and the parameters
 * passed to the methods.
 * @author Sebastien Harispe
 */
public class ValidatorDAG {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    G graph;
    WalkConstraint wc;
    HashMap<URI, Color> verticesColors;
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
    public boolean isDag(G graph, Set<URI> startingURIs, WalkConstraint wc) throws SLIB_Ex_Critic {

        this.wc = wc;

        this.graph = graph;
        this.verticesColors = new HashMap<URI, Color>();
        valid = true;

        logger.debug("Cheking DAG property of : " + graph.getURI());
        logger.debug("WalkConstraint                  : " + wc);


        if (startingURIs == null || startingURIs.isEmpty()) {
            return false;
        }

        logger.debug("starting nodes          : " + startingURIs.size());

        if (startingURIs.size() < 10) {
            logger.debug("starting vertices : " + startingURIs);
        }

        currentPath = new Path();

        for (URI rootUri : startingURIs) {




            if (!graph.containsVertex(rootUri)) {
                throw new SLIB_Ex_Critic("Vertex '" + rootUri + "' not found in " + graph.getURI());
            }

            if (valid) {
                performDFS(rootUri);
            }
        }

        logger.info("isDag : " + valid);
        if (!valid) {

            logger.info("current path :" + currentPath.toString());
            logger.info("Cycle detected adding : " + lastEdge + " to path");
        }
        return valid;
    }

    private void performDFS(URI v) {

        if (!valid) {
            return;
        }

        if (!verticesColors.containsKey(v)) {

            verticesColors.put(v, Color.ORANGE);


            Set<E> edges = graph.getE(v, wc);


            for (E e : edges) {



                if (!valid) {
                    return;
                }

                URI target = e.getTarget();

                if (target.equals(v)) { // IN
                    target = e.getSource();
                }

                if (verticesColors.get(target) != Color.RED) {
                    currentPath.addEdge(e);
                    lastEdge = e;
                    performDFS(target);
                }

            }
            if (!valid) {
                return;
            }
            currentPath.removeLastEdge();
            verticesColors.put(v, Color.RED);

        } else if (verticesColors.get(v) == Color.ORANGE) {
            valid = false;
        }
    }

    /*---------------------------------------------------------------------*
     *  Utils
     *---------------------------------------------------------------------*/
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
    public boolean isDag(G graph, URI rootURI, WalkConstraint wc) throws SLIB_Ex_Critic {
        return isDag(graph, SetUtils.buildSet(rootURI), wc);
    }

    /**
     *
     * @param graph
     * @return
     * @throws SLIB_Ex_Critic
     */
    public boolean containsTaxonomicDag(G graph) throws SLIB_Ex_Critic {
        WalkConstraint wct = new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.IN);
        return isDag(graph, wct);
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
    public boolean isDag(G graph, WalkConstraint wc) throws SLIB_Ex_Critic {

        logger.debug("Check DAG property of the graph "+graph.getURI()+" considering the walkconstraint "+wc);
        Set<URI> startingNodes = getDAGRoots(graph, WalkConstraintUtils.getInverse(wc, false));

        logger.info("Starting process from " + startingNodes.size() + " vertices");

        if (startingNodes.isEmpty()) // No root No Dag
        {
            return false;
        }

        return isDag(graph, startingNodes, wc);
    }

    /**
     * Root vertices (terminal vertices) are those of type CLASS which respect
     * the following restrictions :<br/> <ul> <li> must not contains an edges of
     * the given types and direction </li> </ul>
     *
     * Do not check if the graph is a DAG
     *
     * @param g the graph on which the root vertices need to be retrieve
     * @param etypes e.g. if taxonomic graph use SUPERCLASSOF
     * @param dir
     * @return The set of vertices matching the predefined conditions
     */
    public Set<URI> getDAGRoots(G g, WalkConstraint wc) {

        Set<URI> classes = GraphAccessor.getClasses(g);

        Set<URI> roots = new HashSet<URI>();
        for (URI v : classes) {

            if (g.getV(v, wc).isEmpty()) {
                roots.add(v);
            }
        }
        return roots;
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

        WalkConstraintGeneric wc = new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.OUT);

        Set<URI> roots = getDAGRoots(g, wc);

        logger.info("Number of roots " + roots.size());

        if (roots.size() == 1) {
            isDag(g, roots.iterator().next(), new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.IN));
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
     * @throws IllegalArgumentException if the underlying taxonomic graph of the
     * given graph contains multiple roots.
     *
     * @see
     */
    public URI getRootedTaxonomicDAGRoot(G g) throws SLIB_Ex_Critic {

        WalkConstraintGeneric wc = new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.OUT);
        Set<URI> roots = getDAGRoots(g, wc);

        if (roots.size() != 1) {
            throw new IllegalArgumentException("Multiple roots detected in the underlying taxonomic graph of graph "+g.getURI());
        }

        return roots.iterator().next();
    }

    /**
     * Do not check if the graph is a DAG
     *
     * @param g
     * @return
     */
    public Set<URI> getTaxonomicDAGRoots(G g) {
        return getDAGRoots(g, new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.OUT));
    }

    /**
     * Test if the given URI corresponds to a root of the graph build according
     * to the specified edge types.
     *
     * @param g the graph to consider
     * @param rootURI the URI to test
     * @param edgesType the edge type to consider only OUT direction is
     * considered
     * @return true if the graph defined by the specified constraint is rooted
     * by the given URI
     */
    public boolean containsRootedDagRoot(G g, URI rootURI, WalkConstraint wc) {

        for (URI v : getDAGRoots(g, wc)) {
            if (v.equals(rootURI)) {
                return true;
            }
        }

        return false;
    }

    /**
     * @param g
     * @param root
     * @return
     * @throws SLIB_Ex_Critic
     */
    public boolean isUniqueRootedTaxonomicDag(G g, URI root) throws SLIB_Ex_Critic {

        WalkConstraint wc = new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.IN);
        return isUniqueRootedDagRoot(g, root, wc);
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
    public boolean isUniqueRootedDagRoot(G g, URI root, WalkConstraint wc) throws SLIB_Ex_Critic {

        if (isDag(g, wc)) {

            Set<URI> roots = getDAGRoots(g, WalkConstraintUtils.getInverse(wc, false));

            if (roots.size() == 1 && roots.iterator().next().equals(root)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @return
     */
    public E getLastEdge() {
        return lastEdge;
    }
}
