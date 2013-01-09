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
package slib.sglib.algo.reduction.dag;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.sglib.algo.traversal.classical.DFS;
import slib.sglib.algo.validator.dag.ValidatorDAG;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.utils.Direction;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.impl.SetUtils;

/**
 * Class used to perform a transitive reduction of a DAG see
 * http://en.wikipedia.org/wiki/Transitive_reduction
 *
 * @author Sebastien Harispe
 *
 */
public class GraphReduction_Transitive {

    static Logger logger = LoggerFactory.getLogger(GraphReduction_Transitive.class);

    /**
     * Performs a transitive reduction of the given graph only taxonomic
     * relationships are considered i.e SUBCLASSOF, SUPERCLASSOF The rooted DAG
     * property of the graph is checked considering SUBCLASSOF relationships.
     *
     * @param graph the graph on which the transitive reduction needs to be
     * performed
     * @throws SLIB_Ex_Critic 
     * @return the set of edges removed.
     */
    public static Set<E> process(G graph) throws SLIB_Ex_Critic {

        ValidatorDAG validator = new ValidatorDAG();

        if (!validator.containsRootedTaxonomicDag(graph)) {
            throw new SLIB_Ex_Critic("Transitive reduction require ROOTED DAG");
        }

        V root = new ValidatorDAG().getRootedTaxonomicDAGRoot(graph);

        logger.info("Transitive reduction considering root: " + root);
        return process(graph, root);
    }

    /**
     * Performs a transitive reduction of the given graph considering a given
     * vertex as root only taxonomic relationships are considered i.e
     * SUBCLASSOF, SUPERCLASSOF.
     *
     * @param g 
     * @param src 
     * @return the set of edges removed.
     */
    public static Set<E> process(G g, V src) {


        Set<E> removableEdges = new HashSet<E>();

        logger.info("Processing transitive reduction src: " + src);

        DFS dfs = new DFS(g, src, RDFS.SUBCLASSOF, Direction.IN);

        List<V> topoOrder = dfs.getTraversalOrder();

        HashMap<V, HashSet<V>> reachableV = new HashMap<V, HashSet<V>>();

        for (int i = topoOrder.size() - 1; i >= 0; --i) {

            V currentV = topoOrder.get(i);


            if (!reachableV.containsKey(currentV)) {
                reachableV.put(currentV, new HashSet<V>());
            }

            reachableV.get(currentV).add(currentV);
            Collection<E> edges = g.getE(RDFS.SUBCLASSOF, currentV, Direction.IN);

            for (E e : edges) {

                V target = e.getSource();

                if (!reachableV.containsKey(target)) {
                    reachableV.put(target, new HashSet<V>());
                    reachableV.get(target).addAll(reachableV.get(currentV));
                } else {
                    Collection<V> inter = SetUtils.intersection(reachableV.get(target), reachableV.get(currentV));

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
