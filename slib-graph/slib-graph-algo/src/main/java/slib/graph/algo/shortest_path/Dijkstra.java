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
package slib.graph.algo.shortest_path;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.graph.model.graph.G;
import slib.graph.model.graph.elements.E;
import slib.graph.model.graph.utils.WalkConstraint;
import slib.graph.model.graph.weight.GWS;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 * Basic implementation of the shortest path algorithm proposed by Dijkstra Only
 * suited for shortest path exclusively composed of non-negative weight <a
 * href="http://en.wikipedia.org/wiki/Dijkstra's_algorithm">more about</a> TODO
 * optimize i.e Fibonacci heap implementation
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class Dijkstra {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    G g;
    WalkConstraint walkConstraints;
    GWS ws = null;
    public final static Double NOT_COMPUTED = -1.;

    /**
     * Check the weighting scheme only contains non negative weights
     *
     * @throws SGL_Ex_Critic
     */
    private void checkGWSisNonNegative() throws SLIB_Ex_Critic {

        if (ws != null) {
            for (E e : g.getE(walkConstraints.getAcceptedPredicates())) {
                if (ws.getWeight(e) < 0) {
                    throw new SLIB_Ex_Critic("Dijkstra algorithm cannot be used for a weighting scheme composed of negative weight");
                }
            }
        }
    }

    /**
     * Edge weights set to 1
     *
     * @param g
     * @param walconstraints
     * @throws SLIB_Ex_Critic
     */
    public Dijkstra(G g, WalkConstraint walconstraints) throws SLIB_Ex_Critic {
        this.g = g;
        this.walkConstraints = walconstraints;
        this.ws = null;
    }

    /**
     * Create an Dijkstra algorithm used to compute shortest paths on a graph
     * considering a given non negative weighting scheme
     *
     * @param g the graph on which the shortest path has to be computed
     * @param walconstraints the constraint associated to the search
     * @param weightingScheme a
     * @throws SLIB_Ex_Critic
     */
    public Dijkstra(G g, WalkConstraint walconstraints, GWS weightingScheme) throws SLIB_Ex_Critic {
        this.g = g;
        this.walkConstraints = walconstraints;
        this.ws = weightingScheme;

        checkGWSisNonNegative();
    }

    /**
     * Compute shortest path between two nodes Note also that the resultStack is
     * populated during computation
     *
     * @param source
     * @param t
     * @return the shortest path weight as double
     */
    public Double shortestPath(URI source, URI t) {

        logger.debug("\tComputing Shortest path... from " + source + " to " + t + " " + ws);

        HashMap<URI, Double> dists = new HashMap<URI, Double>();
        HashMap<URI, Boolean> visited = new HashMap<URI, Boolean>();

        // initialize data structures
        for (URI v : g.getV()) {
            dists.put(v, NOT_COMPUTED);
            visited.put(v, false);
        }

        dists.put(source, 0.);

        for (int i = 0; i < dists.size(); i++) {

            if (dists.size() >= 1000 && i % 1000 == 0) {
                logger.info("\tComputing Shortest path... Step " + i + "/" + dists.size());
            }

            URI next = minVertex(dists, visited);

            if (next == null) {
                break;
            } else if (next == t) {
                return dists.get(t);
            }

            visited.put(next, true);

            Set<E> edges = g.getE(next, walkConstraints);

            for (E e : edges) {

                Double d = dists.get(next) + (ws == null ? 1 : ws.getWeight(e));

                URI target = e.getTarget();

                if (!target.equals(next)) { // outEdge
                    if (dists.get(target) == NOT_COMPUTED || dists.get(target) > d) {
                        dists.put(target, d);
                    }
                } else { // in Edges
                    URI src = e.getSource();
                    if (dists.get(src) == NOT_COMPUTED || dists.get(src) > d) {
                        dists.put(src, d);
                    }
                }
            }
        }

        return dists.get(t);
    }

    /**
     *
     * @param source
     * @return a Map containing the shortest path from the given source to the
     * other vertices of the graph.
     */
    public ConcurrentHashMap<URI, Double> shortestPath(URI source) {

        logger.debug("\tComputing Shortest path... from " + source + "  " + ws);

        ConcurrentHashMap<URI, Double> dists = new ConcurrentHashMap<URI, Double>();
        ConcurrentHashMap<URI, Boolean> visited = new ConcurrentHashMap<URI, Boolean>();

        // initialize data structures
        for (URI v : g.getV()) {
            dists.put(v, NOT_COMPUTED);
            visited.put(v, false);
        }

        dists.put(source, new Double(0));

        for (int i = 0; i < dists.size(); i++) {

            if (dists.size() >= 1000 && i % 1000 == 0) {
                logger.debug("\tComputing Shortest from " + source + " paths... Step " + i + "/" + dists.size());
            }

            URI next = minVertex(dists, visited);

            if (next == null) {
                break;
            }

            visited.put(next, true);

            Set<E> edges = g.getE(next, walkConstraints);

            for (E e : edges) {

                Double d = dists.get(next) + (ws == null ? 1 : ws.getWeight(e));

                URI target = e.getTarget();

                if (!target.equals(next)) { // outEdge
                    if (dists.get(target) == NOT_COMPUTED || dists.get(target) > d) {
                        dists.put(target, d);
                    }
                } else { // in Edges
                    URI src = e.getSource();
                    if (dists.get(src) == NOT_COMPUTED || dists.get(src) > d) {
                        dists.put(src, d);
                    }
                }
            }
        }
        return dists;
    }

    private URI minVertex(Map<URI, Double> dist, Map<URI, Boolean> visited) {

        Double x = Double.MAX_VALUE;

        URI next = null;   // graph not connected, or no unvisited vertices

        for (URI v : dist.keySet()) {

            if (!visited.get(v) && dist.get(v) != NOT_COMPUTED && dist.get(v) < x) {
                next = v;
                x = dist.get(v);
            }
        }
        return next;
    }
}
