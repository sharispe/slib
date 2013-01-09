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
package slib.sglib.algo.traversal.classical;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sglib.algo.traversal.GraphTraversal;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.utils.Direction;
import slib.sglib.model.repo.PredicateFactory;
import slib.utils.impl.SetUtils;

/**
 * Class used to perform traversal on a graph using Depth First Search Algorithm
 * from a set of vertices and considering particular type of relationships. <a
 * href="http://en.wikipedia.org/wiki/Depth-first_search">more about</a>
 *
 * Note that contrary to {@link BFS} the traversal is performed at instance
 * creation. The iteration is then made on the stored topological sort. The
 * topological sort can also be retrieve.
 *
 * @author Sebastien Harispe
 *
 */
public class DFS implements GraphTraversal {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    G g;
    Set<V> sources;
    HashMap<V, Boolean> coloredVertex;
    Set<URI> edgesTypes;
    List<V> topoSort;
    Direction dir;
    int current_id = 0;
    boolean removePerformed = false;

    /**
     * Create a DFS iterator, note that DFS is performed at instance creation.
     * The resulting topological sort can be accessed through
     * {@link DFS#getTraversalOrder()}
     *
     * @param g	the graph to consider
     * @param sources the set of vertices {@link V} from which the DFS need to
     * be performed
     * @param edgesTypes the set of {@link EType} to consider during traversal.
     * If set to null only native edge types are considered see
     * {@link IPredicateURIRepo#getEtypeNative(G)} for more information about
     * native edge types.
     *
     * TODO Use WalkConstraint
     * @param dir  
     */
    public DFS(G g, Set<V> sources, Set<URI> edgesTypes, Direction dir) {
        init(g, sources, edgesTypes, dir);
    }

    /**
     * Shortcut of {@link DFS#DFS(G, Set, Set)}
     *
     * @param g
     * @param source
     * @param etype
     * @param dir  
     */
    public DFS(G g, V source, URI etype, Direction dir) {
        this(g, SetUtils.buildSet(source), SetUtils.buildSet(etype), dir);
    }

    /**
     * Shortcut of {@link DFS#DFS(G, Set, Set)}
     *
     * @param g
     * @param source
     * @param edgesTypes
     * @param dir  
     */
    public DFS(G g, V source, Set<URI> edgesTypes, Direction dir) {
        this(g, SetUtils.buildSet(source), edgesTypes, dir);
    }

    private void init(G g, Set<V> sources, Set<URI> edgesTypes, Direction dir) {

        this.g = g;
        this.dir = dir;
        this.sources = sources;
        this.coloredVertex = new HashMap<V, Boolean>();
        this.topoSort = new ArrayList<V>();

        this.edgesTypes = edgesTypes;

        if (logger.isDebugEnabled()) { // avoid large debug information
            String sources_s = "";
            if (sources.size() > 10) {
                int l = 0;
                for (V v : sources) {
                    sources_s += "\t" + v;
                    l++;
                    if (l == 10) {
                        break;
                    }
                }
            } else {
                sources_s = sources.toString();
            }

            logger.debug("Iterator loaded for " + g.getURI() + " from " + sources.size() + " source(s) " + sources_s);
            logger.debug("Considering relationship types " + edgesTypes);
        }

        logger.debug("Start DFS");
        for (V r : sources) {
            performDFS(r);
        }

        current_id = topoSort.size() - 1;
        logger.debug("TopoSort contains " + topoSort.size() + " vertices (on " + g.getNumberVertices() + " graph vertices)");

    }

    private void performDFS(V v) {

        if (!coloredVertex.containsKey(v)) {

            coloredVertex.put(v, true);

            Iterator<E> it = g.getE(edgesTypes, v, dir).iterator();

            while (it.hasNext()) {
                if (dir == Direction.OUT) {
                    performDFS(it.next().getTarget());
                }
                if (dir == Direction.IN) {
                    performDFS(it.next().getSource());
                }
            }
            topoSort.add(v);
        }
    }

    /**
     *
     * @return
     */
    public boolean hasNext() {
        return current_id > 0;
    }

    /**
     *
     * @return
     */
    public V next() {
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
     * topological ordering as an {@link ArrayList} of {@link V}
     */
    public List<V> getTraversalOrder() {
        return topoSort;
    }
}
