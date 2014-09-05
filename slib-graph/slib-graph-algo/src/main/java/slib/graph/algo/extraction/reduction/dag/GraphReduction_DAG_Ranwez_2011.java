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
package slib.graph.algo.extraction.reduction.dag;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.graph.algo.accessor.GraphAccessor;
import slib.graph.algo.traversal.classical.DFS;
import slib.graph.algo.validator.dag.ValidatorDAG;
import slib.graph.model.graph.G;
import slib.graph.model.graph.elements.E;
import slib.graph.model.graph.utils.Direction;
import slib.graph.model.graph.utils.WalkConstraint;
import slib.graph.utils.WalkConstraintGeneric;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Ex_Warning;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.SetUtils;

/**
 * Algorithm used to extract a subgraph from a DAG (Directed Acyclic Graph)
 * <br/> Implementation of Ranwez et al. 2011 algorithm. <br/>
 * original paper: <br/>
 *
 * ﻿Ranwez V, Ranwez S, Janaqi S: Sub-Ontology Extraction Using Hyponym and
 * Hypernym Closure on is-a Directed Acyclic Graphs. IEEE Transactions on
 * Knowledge and Data Engineering 2011, 99:1-14.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class GraphReduction_DAG_Ranwez_2011 {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    G graph;
    G graph_reduction;
    Set<URI> selectedURIs;
    Set<URI> verticesRed;
    List<URI> traversalOrder;
    Set<URI> predicatesTC;
    private Set<URI> predicatesToAdd;

    public GraphReduction_DAG_Ranwez_2011(G graph) throws SLIB_Exception {
        this(graph, SetUtils.buildSet(RDFS.SUBCLASSOF), SetUtils.buildSet(RDFS.SUBCLASSOF), true);
    }

    /**
     * Method used to perform the subGraph extraction of an acyclic graph based
     * on top-down and bottom-up transitive closures.
     *
     * The reduction is performed considering: <br/>
     * <ul>
     * <li>
     * A set of URIs corresponding to the vertices on which must be based the
     * reduction.
     * </li>
     * <li>
     * A collection of predicate (edge type). The reduction is based on the
     * transitive closure considering a top-down and a bottom-up query
     * extensions considering the given predicates to consider as taxonomic
     * predicates.
     * </li>
     * </ul>
     *
     * All directed relationships between of other types of predicates can also
     * be added between the nodes which compose the reduction (in a
     * post-treatment).
     *
     *
     * @param graph	the graph
     * @param predicatesTC set of predicates to consider as taxonomic predicate
     * for the transitive closures
     *
     * @param predicateToAdd defines which predicate relationships must be
     * considered in the post treatment. if nodes x and y compose the reduction
     * and x,y are linked by a relationship of type p, p must be specified in
     * the predicateToAdd set in order to specified that the relationship x,p,y
     * must be expressed in the reduction.
     *
     * @param validateDAGproperty boolean if true DAG property of the graph
     * induced by the given parameters is checked
     * @throws SLIB_Exception
     */
    public GraphReduction_DAG_Ranwez_2011(
            G graph,
            Set<URI> predicatesTC,
            Set<URI> predicateToAdd,
            boolean validateDAGproperty) throws SLIB_Exception {

        this.graph = graph;
        this.predicatesTC = predicatesTC;
        this.predicatesToAdd = predicateToAdd;

        logger.debug("Selected predicate(s): " + predicatesTC);
        logger.debug("Predicate to add (post Treatment): " + predicateToAdd);

        if (validateDAGproperty) {
            checkGraphProperties();
        }
    }

    /**
     * @param selectedURIs
     * @param g_reduction
     * @throws SLIB_Ex_Critic
     * @throws SLIB_Ex_Warning
     */
    public void exec(Set<URI> selectedURIs, G g_reduction) throws SLIB_Ex_Critic, SLIB_Ex_Warning {

        logger.debug("###########################################################################");
        logger.debug(selectedURIs.toString());

        this.graph_reduction = g_reduction;
        this.selectedURIs = selectedURIs;

        verticesRed = new HashSet<URI>();

        logger.debug("Query composed of  " + selectedURIs.size() + " elements");
        logger.debug("Edges types   " + predicatesTC.size() + " elements");

        checkQueryValidity();
        computeTraversalRestriction();

        // Selection of the vertices part of the reduction

        for (URI type : predicatesTC) {
            Set<URI> types = SetUtils.buildSet(type);
            verticesRed.addAll(reduce(traversalOrder, types, Direction.OUT));
            logger.debug("Reduction: "+verticesRed);
            logger.debug("Reduction Vertices : " + verticesRed.size());
        }

        Collections.reverse(traversalOrder);

        for (URI type : predicatesTC) {

            Set<URI> types = SetUtils.buildSet(type);
            verticesRed.addAll(reduce(traversalOrder, types, Direction.IN));
            logger.debug("Reduction: "+verticesRed);
            logger.debug("Reduction Vertices : " + verticesRed.size());
        }

        // Bottom Up
        double vReductionP = 100 - (verticesRed.size() * 100 / graph.getV().size()); //percentage of vertices reduction

        logger.debug("Reduction Vertices : " + verticesRed.size() + " ( ~" + vReductionP + "% of " + graph.getURI() + ")");
        logger.debug("Reduction : " + verticesRed);


        graph_reduction.addV(verticesRed);

        Collections.reverse(traversalOrder);

        logger.debug(traversalOrder.toString());

        for (URI e : predicatesTC) {
            addEdges(traversalOrder, e);
        }


        logger.debug("Adding direct edges considering " + predicatesToAdd);
        logger.debug("Adding direct edges considering " + predicatesToAdd.size() + " eType(s)");
        logger.debug("" + predicatesToAdd);

        // Add Direct edges 
        for (URI v : verticesRed) {

            Collection<E> edgesV = graph.getE(v, Direction.BOTH);


            for (E e : edgesV) {
                if (verticesRed.contains(e.getSource()) && verticesRed.contains(e.getTarget()) && predicatesToAdd.contains(e.getURI())) {
                    graph_reduction.addE(e);
                }
            }
        }


        double eReductionP = 100 - (graph_reduction.getE().size() * 100 / graph.getE().size()); // same for edges
        logger.debug("Reduction Edges 	 : " + verticesRed.size() + " ( ~" + (eReductionP) + "% of " + graph.getURI() + ")");
        logger.info("Reduction performed");
    }

    private void computeTraversalRestriction() throws SLIB_Ex_Critic {
        WalkConstraint wc = new WalkConstraintGeneric();
        for (URI edgesType : predicatesTC) {
            wc.addAcceptedTraversal(edgesType, Direction.IN);
        }

        Set<URI> roots = new HashSet<URI>();

        for (URI uri : GraphAccessor.getClasses(graph)) {

            boolean valid = true;

            for (URI p : predicatesTC) {
                if (!graph.getE(p, uri, Direction.OUT).isEmpty()) { // the URI doesn't refer to a root
                    valid = false;
                }
            }

            if (valid) {
                roots.add(uri);
            }
        }

        if (roots.isEmpty()) {
            throw new SLIB_Ex_Critic("Cannot identify any root...");
        }

        DFS dfs = new DFS(graph, roots, wc);
        traversalOrder = dfs.getTraversalOrder(); // rootID as last element
    }

    private void addEdges(List<URI> traversalOrder, URI edgeType) {

        logger.debug("-------------------------------------------------");
        logger.debug("-------------------------------------------------");
        logger.debug("Adding Edges of transitive type : " + edgeType);
        logger.debug("verticesRed : " + verticesRed);
        logger.debug("Starting from  : " + traversalOrder.get(0));

        HashMap<URI, Collection<URI>> vrra = new HashMap<URI, Collection<URI>>();



        for (int i = 0; i < traversalOrder.size(); i++) {

            URI u = traversalOrder.get(i);

//            logger.debug("**************** Processing  : " + u);


            // leaf or root init
            if (!vrra.containsKey(u)) {
                vrra.put(u, new HashSet<URI>());
            }

//            logger.debug("Set : " + vrra.get(u));

            if (verticesRed.contains(u)) {

                for (URI r : vrra.get(u)) {


                    URI source = u;
                    URI target = r;

                    graph_reduction.addE(target, edgeType, source);
//                    logger.debug("\tAdding edge... " + target + " " + edgeType + " " + source);
                }
                vrra.put(u, new HashSet<URI>());
                vrra.get(u).add(u);
            }

            Collection<E> edges = graph.getE(edgeType, u, Direction.OUT);

            for (E e : edges) {

                URI f = e.getTarget();

                if (!vrra.containsKey(f)) {
                    vrra.put(f, new HashSet<URI>());
                }

                vrra.put(f, SetUtils.union(vrra.get(u), vrra.get(f)));

                if (verticesRed.contains(u)) {
                    vrra.get(f).add(u);
                }
            }

        }
    }

    /**
     * Retrieve the vertices selected during a reduction Used to perform
     * top-down and bottom up expansion of the query vertices
     *
     * @param order an ArrayList of IVertex vertices order to consider during
     * the expansion first element treated first
     * @param edgeTypes a Collection of edge type used for the expansion
     * @return	a collection of vertices corresponding to the expansion of the
     * query
     */
    private Set<URI> reduce(List<URI> order, Set<URI> edgeTypes, Direction dir) {

        logger.debug("-----------------------------------------------------------");
        logger.debug("'Transitive Closure' considering EdgeTypes : " + edgeTypes);
        logger.debug("Direction: " + dir);
        logger.debug("Propogation started from : " + order.get(0));
        logger.debug("Size traversal ordering: "+order.size());

        Map<URI, Set<URI>> sd = new HashMap<URI, Set<URI>>(order.size());
        Map<URI, Integer> maxSd = new HashMap<URI, Integer>();


        Set<URI> verticesReduction = new HashSet<URI>();

        for (URI v : order) {
            sd.put(v, new HashSet<URI>());
            maxSd.put(v, 0);
        }

        for (URI v : order) {
            
            

            if (selectedURIs.contains(v)) {
                sd.get(v).add(v);
            }
            
//            logger.debug("* "+v+" max single gain: "+maxSd.get(v)+" total gain: "+sd.get(v).size());
            
            if (sd.get(v).size() > maxSd.get(v)) {

                verticesReduction.add(v);
//                logger.debug("- Add "+v);
                sd.get(v).add(v);
            }

            for (E e : graph.getE(edgeTypes, v, dir)) {

                URI t;
                if(dir == Direction.OUT){
                    t = e.getTarget();
                }
                else{
                    t = e.getSource();
                }
                

                // check that the vertex is contained in the traversal 
                // restriction defined by the root selected 

                if (sd.containsKey(t)) {

                    Set<URI> union = new HashSet<URI>(SetUtils.union(sd.get(t), sd.get(v)));
                    sd.put(t, union);
                    maxSd.put(t, Math.max(sd.get(v).size(), maxSd.get(t)));
                }
            }
        }
        logger.debug(" "+verticesReduction.toString());
        logger.debug("-reduction contains "+verticesReduction.size());
        return verticesReduction;
    }

    /**
     * Check the parameters of the current configuration
     *
     * @throws SLIB_Ex_Critic
     * @throws SLIB_Ex_Warning
     */
    private void checkQueryValidity() throws SLIB_Ex_Critic, SLIB_Ex_Warning {


        if (selectedURIs == null || selectedURIs.size() < 2) {
            throw new SLIB_Ex_Warning("Warning: Query skipped, a minimim of two URI have to be specified to build a query");
        }

        for (URI uri : selectedURIs) {

            if (!graph.containsVertex(uri)) {
                throw new SLIB_Ex_Warning("No vertex associated to URI: " + uri);
            }
        }
    }

    private void checkGraphProperties() throws SLIB_Ex_Critic {
        logger.debug("Checking DAG property");

        ValidatorDAG vdag = new ValidatorDAG();

        WalkConstraint wc = new WalkConstraintGeneric();
        for (URI edgeType : predicatesTC) {
            wc.addAcceptedTraversal(edgeType, Direction.IN);
        }

        boolean isDag = vdag.isDag(graph, wc);
        logger.debug("is DAG: " + isDag);

        if (!isDag) {
            throw new SLIB_Ex_Critic(
                    "Treatment can only be performed on a DAG, traversal "
                    + "respecting your parameters define a cyclic graph.");
        }

//        ValidatorDAG validator = new ValidatorDAG();
//
//        boolean uniqueRoot = validator.isUniqueRootedTaxonomicDag(graph, rootURI);
//
//        if (!uniqueRoot) {
//            logger.info("Specified root is not a unique Root: " + rootVertex);
//            logger.info("Roots : " + validator.getTaxonomicDAGRoots(graph));
//        }
    }
}
