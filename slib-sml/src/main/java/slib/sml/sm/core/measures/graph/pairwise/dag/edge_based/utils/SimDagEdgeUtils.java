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
package slib.sml.sm.core.measures.graph.pairwise.dag.edge_based.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.openrdf.model.URI;

import slib.graph.algo.shortest_path.Dijkstra;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.SetUtils;

/**
 * Utility class for edge-based measures.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class SimDagEdgeUtils {

    /**
     * Compute the most specific ancestor considering Pekar and Staab 2002
     * formula to minimize i.e. common ancestor minimizing score(c) =
     * minDist_edge(A,c) + minDist_edge(B,c) + minDist_edge(ROOT,c)
     *
     * Pekar V, Staab S: Taxonomy learning: factoring the structure of a
     * taxonomy into a semantic classification decision. In COLING ’02
     * Proceedings of the 19th international conference on Computational
     * linguistics. Association for Computational Linguistics; 2002, 2:1-7.
     *
     * @param root the root of the graph
     * @param allSpA shortest path from A to all concepts
     * @param allSpB	shortest path from B to all concepts
     * @param ancestors_A	inclusive ancestors of A
     * @param ancestors_B	inclusive ancestors of B
     * @param dijkstra
     * @return the vertex corresponding to the Most Specific Ancestors
     */
    public static URI getMSA_pekar_staab(
            URI root,
            Map<URI, Double> allSpA,
            Map<URI, Double> allSpB,
            Set<URI> ancestors_A,
            Set<URI> ancestors_B,
            Dijkstra dijkstra) {

        Set<URI> interSecAncestors = SetUtils.intersection(ancestors_A, ancestors_B);

//		System.out.println("intersec "+interSecAncestors);

        Map<URI, Double> msaSet = new HashMap<URI, Double>(); // eligible MSA + dist to root

        if (interSecAncestors.isEmpty()) {

            // Search MSA considering PK function 

            double scoremin = Double.MAX_VALUE;

            for (URI r : interSecAncestors) {

                double sp_root = dijkstra.shortestPath(root, r);

                double score = allSpA.get(r) + allSpB.get(r) + sp_root;

//				System.out.println("\n\n"+r);
//				System.out.println("SP TO ROOT "+r+"\t"+sp_root);
//				System.out.println("A TO ROOT "+r+"\t"+allSpA.get(r));
//				System.out.println("B TO ROOT "+r+"\t"+allSpB.get(r));
//				System.out.println("V "+r+"\t"+score);

                if (scoremin > score) {
                    scoremin = score;
                    msaSet.clear();
                    msaSet.put(r, sp_root);
                } else if (scoremin == score) {
                    msaSet.put(r, sp_root);
                }
            }
        }

        URI msa = null;
        double msalpr = 0;
        // we select the eligible msa with the longest shortest path to the root (lpr)
        // i.e. Optimistic implementation as more lpr is high, more the final score will be high

        for (Entry<URI, Double> e : msaSet.entrySet()) {

            if (e.getValue() > msalpr) {
                msalpr = e.getValue();
                msa = e.getKey();
            }
        }
        return msa;
    }

    /**
     * Search the Most specific ancestor considering the depth
     *
     * @param interSecAncestors the concept to evaluate
     * @param maxDepths the values which will be used to evaluate the concepts
     * @return the concept maximizing the depth (the first one is retrieve if
     * multiple exist). null is returned if the intersection is empty.
     * @throws SLIB_Exception
     */
    public static URI searchMSA(Set<URI> interSecAncestors,
            Map<URI, Integer> maxDepths) throws SLIB_Exception {


        URI msa = null;
        int dMSA = -1;

        for (URI r : interSecAncestors) {

            if (msa == null || maxDepths.get(r) > dMSA) {
                msa = r;
                dMSA = maxDepths.get(r);
            }
        }
        return msa;
    }
}
