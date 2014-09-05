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
import java.util.Map.Entry;
import java.util.Set;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.graph.algo.traversal.classical.BFS;
import slib.graph.model.graph.G;
import slib.graph.model.graph.utils.WalkConstraint;

/**
 * Reachable Vertex Finder (RVF) objects can be used to retrieve the vertices
 * reachable from a particular vertex of a graph considering particular
 * constraints e.g. type of relationships (i.e. set of predicate URI) particular
 * vertex restriction applied to the walk.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class RVF {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     *
     */
    protected WalkConstraint wc;
    /**
     *
     */
    protected G g;

    /**
     * Reachable vertex Finder
     *
     * @param g
     * @param wc 
     */
    public RVF(G g, WalkConstraint wc) {
        this.g = g;
        this.wc = wc;
    }

    /**
     * Retrieve the set of reachable vertices RV from a specified vertex taking
     * into account the type of relationships associated to the current
     * instance.
     *
     * Exclusive process: the focused vertex will NOT be included in the set of
     * reachable vertices.
     *
     * @param v the focus vertex
     * @return the set of vertices encountered during the traversal.
     */
    public Set<URI> getRV(URI v) {

        List<URI> rv = new ArrayList<URI>();

        BFS it = new BFS(g, v, wc);

        while (it.hasNext()) {

            URI next = it.next();
            rv.add(next);
        }

        rv.remove(v);// The BFS is exclusive

        return new HashSet<URI>(rv);
    }

    /**
     * Compute the set of reachable vertices for each vertices composing the
     * graph associated to the object considering the edge types loaded.
     *
     * Exclusive process: The returned result is exclusive as the queried
     * vertices will NOT be included in their respective set of reachable
     * vertices.
     *
     * @param queryVertices 
     * @return an Map key V value the set of vertices reachable from the key
     */
    public Map<URI, Set<URI>> getRV(Set<URI> queryVertices) {

        logger.debug("Get All reachable vertices : start");

        Map<URI, Set<URI>> allVertices = new HashMap<URI, Set<URI>>();

        for (URI v : queryVertices) {
            allVertices.put(v, getRV(v));
        }

        logger.debug("Get All reachable vertices : end");
        return allVertices;
    }

    /**
     * Return the number of vertices reachable from all vertices composing the
     * graph associated to the instance.
     *
     * Exclusive process: The returned result is exclusive as the queried
     * vertices will NOT be counted as part of their respective set of reachable
     * vertices.
     *
     * @param queryVertices 
     * @return an Map key V value the size of the set of vertices reachable
     * from the key as Integer
     */
    public Map<URI, Integer> getRVnb(Set<URI> queryVertices) {

        Map<URI, Set<URI>> r = getRV(queryVertices);
        Map<URI, Integer> results = new HashMap<URI, Integer>(r.size());

        for (Entry<URI, Set<URI>> entry : r.entrySet()) {
            results.put(entry.getKey(), entry.getValue().size());
        }

        return results;
    }
    
    public WalkConstraint getWalkConstraint(){
        return wc;
    }
    
    public void setWalkConstraint(WalkConstraint nwc){
        this.wc = nwc;
    }
}
