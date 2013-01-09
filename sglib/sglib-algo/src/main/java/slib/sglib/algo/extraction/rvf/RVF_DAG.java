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
package slib.sglib.algo.extraction.rvf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sglib.algo.traversal.classical.BFS;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.utils.Direction;
import slib.sglib.model.graph.utils.WalkConstraints;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.ResultStack;
import slib.utils.impl.SetUtils;

/**
 * Object of this class can be used to retrieve the vertices reachable from a
 * particular vertex of an acyclic graph considering particular relationships
 * i.e. EdgeTypes
 *
 * @author Sebastien Harispe
 *
 */
public class RVF_DAG extends RVF {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Create a basic RVF object considering an acyclic graph and a only one
     * type of relationships to consider during the traversal. Note that graph
     * acyclicity is not evaluated.
     *
     * @param g the Semantic Graph to consider
     * @param eType the edgeType to take into account during the traversals
     */
    public RVF_DAG(G g, WalkConstraints wc) {
        super(g, wc);
    }

    /**
     * Compute the set of reachable vertices for each vertices composing the
     * graph associated to the object considering the edge types Optimized
     * through a topological ordering
     *
     * @return an HashMap key V value the set of vertices reachable from the key
     * Set<V>
     * @throws SGL_Ex_Critic
     */
    public Map<V, Set<V>> getAllVertices() throws SLIB_Ex_Critic {

        logger.debug("Get all reachable vertices : start");
        logger.debug("Walk constraint\n" + wc);

        Map<V, Set<V>> allVertices = new HashMap<V, Set<V>>();

        Map<V, Integer> inDegree = new HashMap<V, Integer>();
        Map<V, Integer> inDegreeDone = new HashMap<V, Integer>();

        // Initialize DataStructure + queue considering setEdgeTypes
        List<V> queue = new ArrayList<V>();

        WalkConstraints oppositeWC = wc.getInverse(false);
        logger.debug("Opposite Walk constraint " + oppositeWC);

        for (V v : g.getV(wc.getAcceptedVTypes())) {

            allVertices.put(v, new HashSet<V>());
            int sizeOpposite = g.getE(v, wc).size();

            inDegree.put(v, sizeOpposite);
            inDegreeDone.put(v, 0);

            if (sizeOpposite == 0) {
                queue.add(v);
            }
        }

        if (queue.isEmpty()) {
            throw new SLIB_Ex_Critic("Walk Constraint are to restrictive to use getAllVertices Method, cannot buil initialized queue");
        }

        logger.debug("queue : " + queue);

        logger.debug("Propagation started from " + queue.size() + " vertices");

        while (!queue.isEmpty()) {



            V current = queue.get(0);

//            logger.debug("Processing " + current);

            queue.remove(0);
            allVertices.get(current).add(current);

            Set<E> edges = g.getE(current, oppositeWC);


            for (E e : edges) {

                Direction dir = oppositeWC.getAssociatedDirection(e.getURI());

                V dest = e.getTarget();
                if (dir == Direction.IN) {
                    dest = e.getSource();
                }



                int done = inDegreeDone.get(dest) + 1;
                inDegreeDone.put(dest, done);

//                logger.debug("\tprop to " + dest+"\t"+done+"/"+inDegree.get(dest));

                // union
                Set<V> union = SetUtils.union(allVertices.get(current), allVertices.get(dest));
                allVertices.put(dest, union);



                if (done == inDegree.get(dest)) {
                    queue.add(dest);
//                    logger.debug("*** Adding "+dest);
                }
            }
        }

        //TOREMOVE 

        logger.debug("Check Treatment coherency");
        long incoherencies = 0;
        for (V c : inDegree.keySet()) {

            if (inDegree.get(c) != inDegreeDone.get(c)) {

                logger.debug("\t" + c.getValue() + "\tIndegree " + inDegree.get(c) + "\t" + inDegreeDone.get(c));
                incoherencies++;
            }
        }
        logger.debug("Incoherencies : "+incoherencies);


        logger.debug("Get All reachable vertices : end");
        return allVertices;
    }

//    /**
//     * Return the set of terminal vertices (leaves) reachable for a specified
//     * vertex
//     *
//     * @param v the source vertex
//     * @return the set of terminal vertices as Set<V>
//     */
//    public Set<V> getTerminalVertices(V v) {
//
//        logger.info("Get Reachable Terminal vertices for " + v);
//
//        Set<V> terminalVertice = new HashSet<V>();
//
//        BFS it = new BFS(g, v, wc);
//
//        while (it.hasNext()) {
//
//            V next = it.next();
//            if (g.getE(wc.getAcceptedPredicates(), next, wc.getAcceptedVTypes(), Direction.OUT).isEmpty()) {
//                terminalVertice.add(next);
//            }
//        }
//
//        return terminalVertice;
//    }

    /**
     * Return the set of terminal vertices (leaves) reachable for all vertices
     * composing the loaded graph
     *
     * @return an HashMap key V, value the set of terminal vertices reachable
     * from the key Set<V>
     */
    public HashMap<V, Set<V>> getTerminalVertices() {
        
        logger.info("Retrieving all reachable leaves");

        HashMap<V, Set<V>> allReachableLeaves = new HashMap<V, Set<V>>();
        HashMap<V, Integer> inDegrees     = new HashMap<V, Integer>();
        HashMap<V, Integer> inDegreesDone = new HashMap<V, Integer>();

        // Retrieve all leaves
        ArrayList<V> queue = new ArrayList<V>();

        for (V v : g.getV(wc.getAcceptedVTypes())) {

            allReachableLeaves.put(v, new HashSet<V>());

            int inDegree = g.getE(wc.getAcceptedPredicates(), v, wc.getAcceptedVTypes(), Direction.IN).size();

            inDegrees.put(v, inDegree);
            inDegreesDone.put(v, 0);

            if (inDegree == 0) {
                queue.add(v);
                allReachableLeaves.get(v).add(v);
            }
        }
        
        logger.info("Propagation of leave counts start from "+queue.size()+" leaves on "+g.getV().size()+" concepts");
        
        long c = 0;
        
        while (!queue.isEmpty()) {
            
            V v = queue.get(0);
            queue.remove(0);
            Set<E> edges = g.getE(wc.getAcceptedPredicates(), v, Direction.OUT);

            //logger.info(c+"/"+g.getV().size()+" "+v.getValue().stringValue());
            c++;
            
            for (E e : edges) {

                V target = e.getTarget();
                int degreeDone = inDegreesDone.get(target).intValue();

                allReachableLeaves.put(target, SetUtils.union(allReachableLeaves.get(target), allReachableLeaves.get(v)));

                inDegreesDone.put(target, degreeDone + 1);

                if (inDegreesDone.get(target).intValue() == inDegrees.get(target).intValue()) {
                    queue.add(target);
                }
            }
        }
        return allReachableLeaves;
    }

    public ResultStack<V, Long> computeNbPathLeadingToAllVertices() throws SLIB_Ex_Critic {

        ResultStack<V, Long> allVertices = new ResultStack<V, Long>();

        for (V v : g.getV(wc.getAcceptedVTypes())) {
            allVertices.add(v, (long) 1);
        }
        return propagateNbOccurences(allVertices);
    }

    /**
     * Method used to compute the number of occurrences associated to each
     * vertex after the propagation of the given number of occurences. The
     * occurrence number are propagated considering a walk defined by the
     * relationships loaded. A number of occurrences is associated to each
     * vertices of the graph through the given inputs. The occurrences number
     * are then summed considering walks starting from the terminal vertices.
     *
     * @param nbOccurrence ResultStack of type Double representing the number of
     * occurrences of each vertices
     * @return ResultStack of type Double representing the number occurrences
     * propagated of each vertices
     * @throws SGL_Ex_Critic
     */
    public ResultStack<V, Long> propagateNbOccurences(ResultStack<V, Long> nbOccurrence) throws SLIB_Ex_Critic {

        HashMap<V, Set<V>> allVertices = new HashMap<V, Set<V>>();
        HashMap<V, Integer> inDegree = new HashMap<V, Integer>();
        HashMap<V, Integer> inDegreeDone = new HashMap<V, Integer>();
        ResultStack<V, Long> nbOcc_prop = new ResultStack<V, Long>();

        for (V v : nbOccurrence.getValues().keySet()) {
            nbOcc_prop.add(v, nbOccurrence.get(v));
        }


        // Initialize DataStructure + queue considering setEdgeTypes
        List<V> queue = new ArrayList<V>();

        for (V v : g.getV(wc.getAcceptedVTypes())) {

            allVertices.put(v, new HashSet<V>());
            int sizeOpposite = g.getE(wc.getAcceptedPredicates(), v, Direction.OUT).size();

            inDegree.put(v, sizeOpposite);
            inDegreeDone.put(v, 0);

            if (sizeOpposite == 0) {
                queue.add(v);
            }
        }

        while (!queue.isEmpty()) {

            V current = queue.get(0);
            queue.remove(0);
            allVertices.get(current).add(current);


            Set<E> edges = g.getE(wc.getAcceptedPredicates(), current, wc.getAcceptedVTypes(), Direction.IN);


            for (E e : edges) {
                V dest = e.getTarget();

                nbOcc_prop.add(dest, nbOcc_prop.get(dest) + nbOcc_prop.get(current));

                int done = inDegreeDone.get(dest) + 1;
                inDegreeDone.put(dest, done);

                // union
                Set<V> union = SetUtils.union(allVertices.get(current), allVertices.get(dest));
                allVertices.put(dest, union);

                if (done == inDegree.get(dest)) {
                    queue.add(dest);
                }
            }
        }
        return nbOcc_prop;
    }
}
