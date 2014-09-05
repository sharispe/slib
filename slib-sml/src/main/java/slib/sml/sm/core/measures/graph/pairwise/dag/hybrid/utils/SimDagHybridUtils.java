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
package slib.sml.sm.core.measures.graph.pairwise.dag.hybrid.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.graph.model.graph.G;
import slib.graph.model.graph.elements.E;
import slib.graph.model.graph.utils.Direction;
import slib.graph.model.graph.weight.GWS;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class SimDagHybridUtils {

    Logger logger = LoggerFactory.getLogger(SimDagHybridUtils.class);

    /**
     * Compute the Semantic contribution for all ancestors of a node Semantic
     * Contribution was defined by Wang et al. 2007
     *
     * Wang JZ, Du Z, Payattakool R, Yu PS, Chen C-F: A new method to
     * measure the semantic similarity of GO terms. Bioinformatics (Oxford,
     * England) 2007, 23:1274-81.
     * @param v
     * @param ancestors
     *
     * @param g
     * @param setEdgeTypes
     * @return map containing the semantic contribution for each URI
     */
    public HashMap<URI, Double> computeSemanticContribution_Wang_2007(
            URI v,
            Set<URI> ancestors,
            G g,
            Set<URI> setEdgeTypes,
            GWS ws) {

//		logger.info("computeSemanticContribution_Wang_2007 for "+v);

//		logger.info("Anc("+v+") = "+ancestors);

        // Initialize Semantic contribution
        // Initialize DataStructure + queue considering setEdgeTypes
        HashMap<URI, Double> sc = new HashMap<URI, Double>();
        HashMap<URI, Integer> inDegree = new HashMap<URI, Integer>();
        HashMap<URI, Integer> inDegreeDone = new HashMap<URI, Integer>();

        /* 
         * In degree have to be recomputed to consider
         * the subgraph composed of all edges/vertices 
         * encountered during a BFS from v to the root
         */

        HashMap<URI, Boolean> visited = new HashMap<URI, Boolean>();

        for (URI c : ancestors) {

            visited.put(c, false);
            inDegree.put(c, 0);
            inDegreeDone.put(c, 0);
            sc.put(c, new Double(0));
        }

        ArrayList<URI> queue = new ArrayList<URI>();
        queue.add(v);
        visited.put(v, true);

        sc.put(v, new Double(1));

        while (!queue.isEmpty()) {

            URI current = queue.get(0);
            queue.remove(0);

            Collection<E> edges = g.getE(setEdgeTypes, current, Direction.OUT);

            for (E e : edges) {
                URI dest = e.getTarget();
                inDegree.put(dest, inDegree.get(dest) + 1);

                if (!visited.get(dest)) {
                    queue.add(dest);
                    visited.put(dest, true);
                }
            }
        }

        // use BFS to propagate Semantic Contribution

        queue = new ArrayList<URI>();
        queue.add(v);



        while (!queue.isEmpty()) {

            URI current = queue.get(0);
            queue.remove(0);

            Collection<E> edges = g.getE(setEdgeTypes, current, Direction.OUT);

            for (E e : edges) {

                URI dest = e.getTarget();
                int done = inDegreeDone.get(dest) + 1;
                inDegreeDone.put(dest, done);

                double new_sc = ws.getWeight(e) * sc.get(current);

                if (sc.get(dest) < new_sc) {
                    sc.put(dest, new_sc);
                }

                if (done == inDegree.get(dest)) {
                    queue.add(dest);
                }
            }
        }
        return sc;
    }

    /**
     * Compute the Semantic Value of a node considering all semantic
     * contribution Semantic Value (SV) was defined by Wang et al. 2007 page
     * 1276
     *
     * Wang JZ, Du Z, Payattakool R, Yu PS, Chen C-F: A new method to
     * measure the semantic similarity of GO terms. Bioinformatics (Oxford,
     * England) 2007, 23:1274-81.
     *
     * @param sc
     * @return double semantic value
     */
    public double computeSV_Wang_2007(Map<URI, Double> sc) {

        double sum = 0;

        for (URI r : sc.keySet()) {
            sum += sc.get(r);
        }

        return sum;
    }
}
