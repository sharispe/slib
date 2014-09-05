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
package slib.graph.algo.metric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.openrdf.model.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.graph.model.graph.G;
import slib.graph.model.graph.elements.E;
import slib.graph.model.graph.utils.Direction;
import slib.graph.model.graph.utils.WalkConstraint;
import slib.graph.utils.WalkConstraintUtils;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 * Class used to analyze depth of vertices composing an acyclic graph
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 *
 */
public class DepthAnalyserAG {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    G g;
    WalkConstraint wc;

    /**
     * Create a DepthAnalyserAG object considering a particular acyclic graph
     * and set of edge types Note that graph acyclicity is not evaluated.
     *
     * @param g The graph to consider
     * @param wc
     */
    public DepthAnalyserAG(G g, WalkConstraint wc) {

        this.wc = wc;

        this.g = g;
    }

    /**
     * Compute the depth of each vertices.
     *
     * @param boolean defining if minimal or maximal depth have to be computed.
     * For max value set the value to true.
     * @return a ResultStack containing the depth of each vertex.
     * @throws SGL_Ex_Critic
     */
    private Map<URI, Integer> getVDepths(boolean max) throws SLIB_Ex_Critic {

        Map<URI, Integer> computedDepths = new HashMap<URI, Integer>();

        Map<URI, Integer> inDegree = new HashMap<URI, Integer>();
        Map<URI, Integer> inDegreeDone = new HashMap<URI, Integer>();

        // Initialize DataStructure + queue considering setEdgeTypes
        List<URI> queue = new ArrayList<URI>();

        logger.debug("Walk constraint loaded " + wc);

        WalkConstraint wcOpp = WalkConstraintUtils.getInverse(wc, false);
        logger.debug("Building initial queue considering inverse constraint " + wcOpp);

        for (URI v : g.getV()) {

            int sizeOpposite = g.getE(v, wcOpp).size();

            computedDepths.put(v, 0);

            inDegree.put(v, sizeOpposite);
            inDegreeDone.put(v, 0);

            if (sizeOpposite == 0) {
                queue.add(v);
            }

        }

        logger.debug("Queue size " + queue.size());


        while (!queue.isEmpty()) {

            URI current = queue.get(0);
            queue.remove(0);


            Set<E> edges = g.getE(current, wc);

            int currentDepth = computedDepths.get(current) + 1;

            for (E e : edges) {

                Direction dir = wc.getAssociatedDirection(e.getURI());
                URI dest = e.getTarget();
                if (dir == Direction.IN) {
                    dest = e.getSource();
                }


                int done = inDegreeDone.get(dest) + 1;
                inDegreeDone.put(dest, done);

                if (computedDepths.get(dest) == 0) {
                    computedDepths.put(dest, currentDepth);
                } else {
                    int computedDepth = computedDepths.get(dest).intValue();

                    if (max) {
                        if (computedDepth < currentDepth) {
                            computedDepths.put(dest, currentDepth);
                        }
                    } else {
                        if (computedDepth > currentDepth) {
                            computedDepths.put(dest, currentDepth);
                        }
                    }
                }
                if (done == inDegree.get(dest)) {
                    queue.add(dest);
                }
            }
        }
        return computedDepths;
    }

    /**
     * @return a ResultStack containing the maximal depth of each vertex.
     * @throws SLIB_Ex_Critic
     */
    public Map<URI, Integer> getVMaxDepths() throws SLIB_Ex_Critic {

        logger.debug("Computing max depths...");
        return getVDepths(true);
    }

    /**
     * @return a ResultStack containing the minimal depth of each vertex.
     * @throws SLIB_Ex_Critic
     */
    public Map<URI, Integer> getVMinDepths() throws SLIB_Ex_Critic {

        logger.debug("Computing min depths...");
        return getVDepths(false);
    }

    /**
     * @return a HashMap representing the distribution of each represented
     * minimal depths. key Integer the depth, value the number of vertices with
     * the corresponding depth
     * @throws SLIB_Exception
     */
    public Map<Integer, Integer> getMinDepthsDistribution() throws SLIB_Exception {

        Map<URI, Integer> allDepths = getVMinDepths();
        Map<Integer, Integer> distribution = getDistribution(allDepths);

        return distribution;

    }

    /**
     * @return a HashMap representing the distribution of each represented
     * maximal depths. key Integer the depth, value the number of vertices with
     * the corresponding depth
     * @throws SLIB_Exception
     */
    public Map<Integer, Integer> getMaxDepthsDistribution() throws SLIB_Exception {

        Map<URI, Integer> allDepths = getVMaxDepths();
        Map<Integer, Integer> distribution = getDistribution(allDepths);

        return distribution;
    }

    /**
     * @param depths a ResultStack containing the depth of each vertex.
     * @return a HashMap representing the distribution of each represented
     * depths. key Integer the depth, value the number of vertices with the
     * corresponding depth
     */
    private <N extends Number> Map<N, Integer> getDistribution(Map<URI, N> depths) {

        Map<N, Integer> distribution = new HashMap<N, Integer>();

        for (Entry<URI, N> entry : depths.entrySet()) {

            N cDepth = entry.getValue();

            Integer nbV = distribution.get(cDepth);

            if (nbV == null) {
                distribution.put(cDepth, 1);
            } else {
                distribution.put(cDepth, nbV + 1);
            }
        }
        return distribution;
    }
}
