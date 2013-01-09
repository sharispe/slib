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
package slib.sglib.algo.metric;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.utils.Direction;
import slib.sglib.model.graph.utils.WalkConstraints;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.ResultStack;

import slib.sglib.model.repo.DataFactory;

/**
 * Class used to analyze depth of vertices composing an acyclic graph
 *
 * @author Sebastien Harispe
 *
 */
public class DepthAnalyserAG {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    DataFactory factory;
    G g;
    WalkConstraints wc;

    /**
     * Create a DepthAnalyserAG object considering a particular acyclic graph
     * and set of edge types Note that graph acyclicity is not evaluated.
     *
     * @param factory 
     * @param g The graph to consider
     * @param wc 
     */
    public DepthAnalyserAG(DataFactory factory, G g, WalkConstraints wc) {

        this.factory = factory;
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
    private ResultStack<V, Integer> getVDepths(boolean max) throws SLIB_Ex_Critic {

        ResultStack<V, Integer> computedDepths = new ResultStack<V, Integer>();

        HashMap<V, Integer> inDegree = new HashMap<V, Integer>();
        HashMap<V, Integer> inDegreeDone = new HashMap<V, Integer>();

        // Initialize DataStructure + queue considering setEdgeTypes
        ArrayList<V> queue = new ArrayList<V>();

        WalkConstraints wcOpp = wc.getInverse(false);

        for (V v : g.getVClass()) {

            int sizeOpposite = g.getE(v, wcOpp).size();

            computedDepths.add(v, 0);

            inDegree.put(v, sizeOpposite);
            inDegreeDone.put(v, 0);

            if (sizeOpposite == 0) {
                queue.add(v);
            }

        }


        while (!queue.isEmpty()) {

            V current = queue.get(0);
            queue.remove(0);


            Set<E> edges = g.getE(current, wc);

            int currentDepth = computedDepths.get(current) + 1;

            for (E e : edges) {

                Direction dir = wc.getAssociatedDirection(e.getURI());
                V dest = e.getTarget();
                if (dir == Direction.IN) {
                    dest = e.getSource();
                }


                int done = inDegreeDone.get(dest) + 1;
                inDegreeDone.put(dest, done);

                if (computedDepths.get(dest) == 0) {
                    computedDepths.add(dest, currentDepth);
                } else {
                    int computedDepth = computedDepths.get(dest).intValue();

                    if (max) {
                        if (computedDepth < currentDepth) {
                            computedDepths.add(dest, currentDepth);
                        }
                    } else {
                        if (computedDepth > currentDepth) {
                            computedDepths.add(dest, currentDepth);
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
    public ResultStack<V, Integer> getVMaxDepths() throws SLIB_Ex_Critic {

        logger.debug("Compute max depths");
        return getVDepths(true);
    }

    /**
     * @return a ResultStack containing the minimal depth of each vertex.
     * @throws SLIB_Ex_Critic 
     */
    public ResultStack<V, Integer> getVMinDepths() throws SLIB_Ex_Critic {

        logger.debug("Compute min depths");
        return getVDepths(false);
    }

    /**
     * @return a HashMap representing the distribution of each represented
     * minimal depths. key Integer the depth, value the number of vertices with
     * the corresponding depth
     * @throws SLIB_Exception  
     */
    public HashMap<Integer, Integer> getMinDepthsDistribution() throws SLIB_Exception {

        ResultStack<V, Integer> allDepths = getVMinDepths();
        HashMap<Integer, Integer> distribution = getDistribution(allDepths);

        return distribution;

    }

    /**
     * @return a HashMap representing the distribution of each represented
     * maximal depths. key Integer the depth, value the number of vertices with
     * the corresponding depth
     * @throws SLIB_Exception 
     */
    public HashMap<Integer, Integer> getMaxDepthsDistribution() throws SLIB_Exception {

        ResultStack<V, Integer> allDepths = getVMaxDepths();
        HashMap<Integer, Integer> distribution = getDistribution(allDepths);

        return distribution;
    }

    /**
     * @param depths a ResultStack containing the depth of each vertex.
     * @return a HashMap representing the distribution of each represented
     * depths. key Integer the depth, value the number of vertices with the
     * corresponding depth
     */
    private <N extends Number> HashMap<N, Integer> getDistribution(ResultStack<V, N> depths) {

        HashMap<N, Integer> distribution = new HashMap<N, Integer>();

        for (Entry<V, N> entry : depths.entrySet()) {

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
