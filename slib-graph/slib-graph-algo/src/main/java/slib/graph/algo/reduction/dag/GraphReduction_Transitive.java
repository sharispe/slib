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
package slib.graph.algo.reduction.dag;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.graph.algo.traversal.classical.DFS;
import slib.graph.algo.validator.dag.ValidatorDAG;
import slib.graph.model.graph.G;
import slib.graph.model.graph.elements.E;
import slib.graph.model.graph.utils.Direction;
import slib.graph.model.graph.utils.WalkConstraint;
import slib.graph.utils.WalkConstraintGeneric;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.impl.SetUtils;

/**
 * Class used to perform a transitive reduction of a DAG see
 * http://en.wikipedia.org/wiki/Transitive_reduction
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 *
 */
public class GraphReduction_Transitive {

    static Logger logger = LoggerFactory.getLogger(GraphReduction_Transitive.class);

    /**
     * Performs a transitive reduction of the underlying taxonomic graph of the
     * given graph. The underlying taxonomic graph is defined based on the
     * rdfs:SubClassOf relationship.
     *
     * @param graph the graph on which the transitive reduction needs to be
     * performed
     * @throws SLIB_Ex_Critic
     * @return the set of edges removed.
     */
    public static Set<E> process(G graph) throws SLIB_Ex_Critic {

        // remove self loops
        int selfLoops = 0;
        for (E e : graph.getE(RDFS.SUBCLASSOF)) {
            if (e.getSource().equals(e.getTarget())) {
                graph.removeE(e);
                selfLoops++;
            }
        }
        if (selfLoops != 0) {
            logger.info(selfLoops + " self loops have been removed");
        }

        ValidatorDAG validator = new ValidatorDAG();

        if (!validator.containsTaxonomicDag(graph)) {
            throw new SLIB_Ex_Critic("Transitive reduction on taxonomic graph requires an underlying DAG to be defined");
        }

        Set<URI> roots = new ValidatorDAG().getTaxonomicRoots(graph);

        logger.info("Transitive reduction considering " + roots.size() + " root(s)");
        logger.debug("roots: " + roots);
        return process(graph, roots);
    }

    /**
     * Performs a transitive reduction of the given graph considering a set of
     * vertices corresponding to the roots of the graph. Only taxonomic
     * relationships are considered i.e SUBCLASSOF.
     *
     * @param g
     * @param srcs the vertex considered as roots
     * @return the set of edges removed.
     */
    public static Set<E> process(G g, Set<URI> srcs) {

        Set<E> removableEdges = new HashSet<E>();

        logger.info("Processing transitive reduction: ");
        logger.debug("Number of roots" + srcs.size() + " root(s)");
        logger.debug("roots: " + srcs);

        WalkConstraint wc = new WalkConstraintGeneric(RDFS.SUBCLASSOF, Direction.IN);
        DFS dfs = new DFS(g, srcs, wc);

        List<URI> topoOrder = dfs.getTraversalOrder();

        HashMap<URI, HashSet<URI>> reachableV = new HashMap<URI, HashSet<URI>>();

        for (int i = topoOrder.size() - 1; i >= 0; --i) {

            URI currentV = topoOrder.get(i);

            if (!reachableV.containsKey(currentV)) {
                reachableV.put(currentV, new HashSet<URI>());
            }

            reachableV.get(currentV).add(currentV);
            Collection<E> edges = g.getE(RDFS.SUBCLASSOF, currentV, Direction.IN);

            for (E e : edges) {

                URI target = e.getSource();

                if (!reachableV.containsKey(target)) {
                    reachableV.put(target, new HashSet<URI>());
                    reachableV.get(target).addAll(reachableV.get(currentV));
                } else {
                    Collection<URI> inter = SetUtils.intersection(reachableV.get(target), reachableV.get(currentV));

                    Collection<E> outTarget = g.getE(RDFS.SUBCLASSOF, target, Direction.OUT);

                    for (E eTarget : outTarget) {
                        if (inter.contains(eTarget.getTarget())) {
                            removableEdges.add(eTarget);
                        }
                    }
                    reachableV.get(target).addAll(reachableV.get(currentV));
                }
            }
        }
        g.removeE(removableEdges);

        if (logger.isDebugEnabled()) {
            for (E e : removableEdges) {
                logger.debug("TODEL : " + e);
            }
        }

        logger.info("Deletion of " + removableEdges.size() + " subClassOf relationships");
        return removableEdges;
    }
}
