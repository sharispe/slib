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
package slib.graph.algo.extraction.rvf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import slib.graph.model.graph.G;
import slib.graph.model.graph.elements.E;
import slib.graph.model.graph.utils.Direction;
import slib.graph.model.graph.utils.WalkConstraint;
import slib.graph.utils.WalkConstraintUtils;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.impl.SetUtils;

/**
 * Object of this class can be used to retrieve the vertices reachable from a
 * particular vertex of an acyclic graph considering particular relationships
 * i.e. EdgeTypes
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 *
 */
public class RVF_DAG extends RVF {

    /**
     * Create a basic RVF object considering an acyclic graph and a only one
     * type of relationships to consider during the traversal. Note that graph
     * acyclicity is required to ensure coherency but is not evaluated.
     *
     * @param g the Semantic Graph to consider
     * @param wc the walk constraint defining the way to reach the vertices
     * which must be returned by the object
     */
    public RVF_DAG(G g, WalkConstraint wc) {
        super(g, wc);
    }

    /**
     * Compute the set of reachable vertices for each vertices contained in the
     * graph according to the specified constraint associated to the instance in
     * use. Exclusive process, i.e. the process doesn't consider that vertex v
     * is contained in the set of reachable vertices from v.
     *
     * Optimized through a topological ordering
     *
     * @return an Map key V value the set of vertices reachable from the key
     * @throws SLIB_Ex_Critic
     */
    public Map<URI, Set<URI>> getAllRV() throws SLIB_Ex_Critic {

        logger.debug("Get all reachable vertices : start");
        logger.debug("Walk constraint\n" + wc);

        Map<URI, Set<URI>> allVertices = new HashMap<URI, Set<URI>>();

        Map<URI, Integer> inDegree = new HashMap<URI, Integer>();
        Map<URI, Integer> inDegreeDone = new HashMap<URI, Integer>();

        // Initialize DataStructure + queue considering walk constraint
        List<URI> queue = new ArrayList<URI>();

        WalkConstraint oppositeWC = WalkConstraintUtils.getInverse(wc, false);
        logger.debug("Opposite Walk constraint " + oppositeWC);

        for (URI v : g.getV()) {

            allVertices.put(v, new HashSet<URI>());

            int sizeOpposite = 0;
            for (E e : g.getE(v, wc)) {
                if (!e.getSource().equals(e.getTarget())) { // avoid self-loop
                    sizeOpposite++;
                }
            }

            inDegree.put(v, sizeOpposite);
            inDegreeDone.put(v, 0);

            if (sizeOpposite == 0) {
                queue.add(v);
            }
        }

        if (queue.isEmpty()) {
            throw new SLIB_Ex_Critic("Walk Constraint are to restrictive to use getAllVertices Method, cannot buil initialized queue..."
                    + "Cannot find terminal vertices, i.e. vertices with no reachable vertices considering walkContraint: \n" + wc + "\nNumber of vertices tested " + allVertices.size());
        }

        logger.debug("Propagation started from " + queue.size() + " vertices");
        if (queue.size() <= 10) {
            logger.debug(queue.toString());
        }

        while (!queue.isEmpty()) {

            URI current = queue.get(0);

//            logger.debug("Processing " + current);
            queue.remove(0);

            Set<E> edges = g.getE(current, oppositeWC);

            for (E e : edges) {

                Direction dir = oppositeWC.getAssociatedDirection(e.getURI());

                URI dest = e.getTarget();
                if (dir == Direction.IN) {
                    dest = e.getSource();
                }
                if (dest.equals(current)) {
                    continue;// avoid self-loop
                }
                int done = inDegreeDone.get(dest) + 1;
                inDegreeDone.put(dest, done);

                // union
                Set<URI> union = SetUtils.union(allVertices.get(current), allVertices.get(dest));
                union.add(current);
                allVertices.put(dest, union);

                if (done == inDegree.get(dest)) {
                    queue.add(dest);
                }
            }
        }

        //TOREMOVE 
        logger.info("Checking Treatment coherency");
        long incoherencies = 0;
        for (URI c : inDegree.keySet()) {

            if (!inDegree.get(c).equals(inDegreeDone.get(c))) {

                if (incoherencies == 0) {
                    logger.debug("\tURI\tIndegree\tInDegreeDone");
                }
                logger.debug("\t" + c + "\tIndegree " + inDegree.get(c) + "\t" + inDegreeDone.get(c));
                incoherencies++;
            }
        }
        logger.info("Incoherencies : " + incoherencies);
        if (incoherencies != 0) {
            String incoherenceMessage = "incoherences found during a treatment, "
                    + "this can be due to incoherences with regard to the graph properties "
                    + "expected by the treatment performed. "
                    + "Please check the processed graph is acyclic, i.e. is a Directed Acyclic Graph.";
            throw new SLIB_Ex_Critic("ERROR " + incoherenceMessage);
        }

        logger.debug("Get All reachable vertices : end");
        return allVertices;
    }

    /**
     * Return the set of terminal vertices (leaves) reachable. Only the nodes
     * which are involved in a relationships which is accepted in the global
     * configuration will be evaluated. Self-loop are not considered. Therefore
     * if p is an accepted predicate if a node i is only involved in a
     * relationship i p i, it will be considered has a leave.
     *
     * It is important to stress that only nodes involved in the considered
     * relationships will be considered. Therefore if the RVF is set to
     * rdfs:subClassOf out all nodes which are not associated to an
     * rdfs:subClassOf relationship will not be processed (even if they are
     * associated to rdf:type relationships). This is important for instance if
     * you use such a method to find all the leaves which are subsumed by a
     * specific class. In this case if the DAF is not rooted you can have
     * isolated class (which does not have ancestors or descendants) which are
     * not considered in the results provided by this method.
     *
     * @return the leaves for each vertices
     */
    public Map<URI, Set<URI>> getTerminalVertices() {

        logger.info("Retrieving all reachable leaves");

        Map<URI, Set<URI>> allReachableLeaves = new HashMap<URI, Set<URI>>();
        Map<URI, Integer> inDegrees = new HashMap<URI, Integer>();
        Map<URI, Integer> inDegreesDone = new HashMap<URI, Integer>();

        // Retrieve all leaves
        List<URI> queue = new ArrayList<URI>();

        Set<URI> studiedURIs = new HashSet<URI>();
        for (E e : g.getE(wc.getAcceptedPredicates())) {
            studiedURIs.add(e.getSource());
            studiedURIs.add(e.getTarget());
        }

        for (URI v : studiedURIs) {

            allReachableLeaves.put(v, new HashSet<URI>());

            int inDegree = 0;
            // we do not count self-loop
            for (E e : g.getE(wc.getAcceptedPredicates(), v, Direction.IN)) {
                if (!e.getSource().equals(v)) {
                    inDegree++;
                }
            }

//            logger.debug(v + "\t in " + inDegree + "\t" + g.getE(wc.getAcceptedPredicates(), v, Direction.IN));
            inDegrees.put(v, inDegree);
            inDegreesDone.put(v, 0);

            if (inDegree == 0) {
                queue.add(v);
                allReachableLeaves.get(v).add(v);
            }
        }

        logger.info("Propagation of leave counts start from " + queue.size() + " leaves on " + g.getV().size() + " concepts");
        logger.debug("Leaves: " + queue);

//        long c = 0;
        while (!queue.isEmpty()) {

            URI v = queue.get(0);
            queue.remove(0);
            Set<E> edges = g.getE(wc.getAcceptedPredicates(), v, Direction.OUT);

            //logger.info(c+"/"+g.getV().size()+" "+v.getValue().stringValue());
//            c++;
            for (E e : edges) {

                URI target = e.getTarget();
                if (target.equals(v)) {
                    continue;
                }
                int degreeDone = inDegreesDone.get(target);

                allReachableLeaves.put(target, SetUtils.union(allReachableLeaves.get(target), allReachableLeaves.get(v)));

                inDegreesDone.put(target, degreeDone + 1);

                if (inDegreesDone.get(target).equals(inDegrees.get(target))) {
                    queue.add(target);
                }
            }

            //logger.debug(v+"\t-- "+allReachableLeaves.get(v));
        }
        return allReachableLeaves;
    }

    /**
     *
     * @return @throws SLIB_Ex_Critic
     */
    public Map<URI, Integer> computeNbPathLeadingToAllVertices() throws SLIB_Ex_Critic {

        Map<URI, Integer> allVertices = new HashMap<URI, Integer>();

        for (URI v : g.getV()) {
            allVertices.put(v, 1);
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
     * @throws SLIB_Ex_Critic
     */
    public Map<URI, Integer> propagateNbOccurences(Map<URI, Integer> nbOccurrence) throws SLIB_Ex_Critic {

        Map<URI, Set<URI>> allVertices = new HashMap<URI, Set<URI>>();
        Map<URI, Integer> inDegree = new HashMap<URI, Integer>();
        Map<URI, Integer> inDegreeDone = new HashMap<URI, Integer>();
        Map<URI, Integer> nbOcc_prop = new HashMap<URI, Integer>();

        for (URI v : nbOccurrence.keySet()) {
            nbOcc_prop.put(v, nbOccurrence.get(v));
        }
        // Initialize DataStructure + queue considering setEdgeTypes
        List<URI> queue = new ArrayList<URI>();

        for (URI v : g.getV()) {

            allVertices.put(v, new HashSet<URI>());
            int sizeOpposite = g.getE(wc.getAcceptedPredicates(), v, Direction.OUT).size();
            inDegree.put(v, sizeOpposite);
            inDegreeDone.put(v, 0);

            if (sizeOpposite == 0) {
                queue.add(v);
            }
        }

        while (!queue.isEmpty()) {

            URI current = queue.get(0);
            queue.remove(0);
            allVertices.get(current).add(current);

            Set<E> edges = g.getE(wc.getAcceptedPredicates(), current, Direction.IN);

            for (E e : edges) {
                URI dest = e.getTarget();

                nbOcc_prop.put(dest, nbOcc_prop.get(dest) + nbOcc_prop.get(current));

                int done = inDegreeDone.get(dest) + 1;
                inDegreeDone.put(dest, done);

                // union
                Set<URI> union = SetUtils.union(allVertices.get(current), allVertices.get(dest));
                allVertices.put(dest, union);

                if (done == inDegree.get(dest)) {
                    queue.add(dest);
                }
            }
        }
        return nbOcc_prop;
    }
}
