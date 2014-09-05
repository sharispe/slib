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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.graph.algo.traversal.GraphTraversal;
import slib.graph.model.graph.G;
import slib.graph.model.graph.elements.E;
import slib.graph.model.graph.utils.WalkConstraint;
import slib.utils.impl.SetUtils;

/**
 * Class used to perform traversal on a graph using Depth First Search Algorithm
 * from a set of vertices and considering particular types of relationships. <a
 * href="http://en.wikipedia.org/wiki/Depth-first_search">more about</a>
 *
 * Note that contrary to {@link BFS} the traversal is performed at instance
 * creation. The iteration is then made on the stored topological sort. The
 * topological sort can also be retrieve.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 *
 */
public class DFS implements GraphTraversal {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    G g;
    Set<URI> sources;
    HashMap<URI, Boolean> coloredVertex;
    private WalkConstraint wc;
    List<URI> topoSort;
    int current_id = 0;
    boolean removePerformed = false;

    /**
     * Create a DFS iterator, note that DFS is performed at instance creation.
     * The resulting topological sort can be accessed through
     */
    public DFS(G g, Set<URI> sources, WalkConstraint wc) {
        this.g = g;
        this.sources = sources;
        this.wc = wc;
        init();
    }

    public DFS(G g, URI source, WalkConstraint wc) {
        this(g, SetUtils.buildSet(source), wc);
    }

    private void init() {

        this.coloredVertex = new HashMap<URI, Boolean>();
        this.topoSort = new ArrayList<URI>();

        logger.debug("Iterator loaded for " + g.getURI() + " from " + sources.size() + " source(s) " + sources);
        logger.debug("Considering Walkconstraint " + wc);
        logger.debug("Start DFS");

        for (URI r : sources) {
            performDFS(r);
        }

        current_id = topoSort.size() - 1;
        logger.debug("TopoSort contains " + topoSort.size() + " vertices (on " + g.getNumberVertices() + " graph vertices)");

    }

    private void performDFS(URI v) {

        if (!coloredVertex.containsKey(v)) {

            coloredVertex.put(v, true);


            Iterator<E> it = g.getE(v, wc).iterator();

            while (it.hasNext()) {
                E e = it.next();
                if (!e.getTarget().equals(v)) {
                    performDFS(e.getTarget());
                } else {
                    performDFS(e.getSource());
                }
            }
            topoSort.add(v);
        }
    }

    @Override
    public boolean hasNext() {
        return current_id > 0;
    }

    @Override
    public URI next() {
        removePerformed = false;
        current_id--;
        return topoSort.get(current_id + 1);
    }

    /**
     * Returns the traversal ordering resulting of the DFS i.e topological sort.
     * Note that the returned value corresponds to the data structure on which
     * relies the iterator. Modification of the data structure will directly
     * impact DFS behavior and coherence.
     *
     * @return the data structure on which the iterator relies i.e. precomputed
     * topological ordering as a List of URI.
     */
    public List<URI> getTraversalOrder() {
        return topoSort;
    }
}
