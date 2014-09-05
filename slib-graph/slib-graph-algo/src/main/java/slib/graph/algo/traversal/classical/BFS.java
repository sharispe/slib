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
package slib.graph.algo.traversal.classical;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.openrdf.model.URI;
import slib.graph.algo.traversal.GraphTraversal;
import slib.graph.model.graph.G;
import slib.graph.model.graph.utils.WalkConstraint;
import slib.utils.impl.SetUtils;

/**
 * Class used to perform traversal on a graph using Breadth First Search (BFS).
 * Algorithm start from a set of vertices and can be tuned to only consider
 * particular types of relationships.
 *
 * <b>Important: </b>The graph is not expected to be modified during the
 * traversal. Traversal is made on the fly i.e. iteratively next calls to the
 * next methods.
 *
 * <a href="http://en.wikipedia.org/wiki/Breadth-first_search">more about
 * BFS</a>
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 *
 */
public class BFS implements GraphTraversal {

    G g;
    private WalkConstraint wc;
    URI current;
    List<URI> queue;
    Set<URI> visited;

    /**
     * Creates an instance of BFS used to perform a Bread First Search Traversal
     * over the graph. The first elements of the iteration will be the sources.
     * The graph is not expected to be modified during the traversal.
     *
     * @param g the graph to consider
     * @param sources the set of vertices considered as sources of the traversal
     * i.e. from which the traversal start
     * @param wc the constraint applied to the walk
     */
    public BFS(G g, Set<URI> sources, WalkConstraint wc) {

        this.g = g;
        this.wc = wc;

        this.queue = new ArrayList<URI>(sources);

        visited = new HashSet<URI>();

    }

    /**
     * Creates an instance of BFS used to perform a Bread First Search Traversal
     * over the graph. The first element of the iteration will be the source of
     * the BFS The graph is not expected to be modified during the traversal.
     *
     * @param g the graph to consider
     * @param source the source of the traversal i.e. from which the traversal
     * start
     * @param wc the constraint applied to the walk
     */
    public BFS(G g, URI source, WalkConstraint wc) {
        this(g, SetUtils.buildSet(source), wc);
    }

    /**
     * Check if the traversal is finished.
     *
     * @return is the BFS finished
     */
    @Override
    public boolean hasNext() {
        return queue.isEmpty() == false;
    }

    /**
     * Access to the next vertex reached by the BFS.
     *
     * @return the next vertex.
     */
    @Override
    public URI next() {

        URI src = queue.get(0);
        queue.remove(0);

        Set<URI> vertices = g.getV(src, wc);

        for (URI v : vertices) {

            if (!visited.contains(v)) {
                queue.add(v);
                visited.add(v);
            }
        }
        current = src;
        return src;
    }
}
