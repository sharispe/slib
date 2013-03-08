/*
 * 
 * Copyright or © or Copr. Ecole des Mines d'Alès (2012) 
 * LGI2P research center
 * This software is governed by the CeCILL  license under French law and
 * abiding by the rules of distribution of free software.  You can  use, 
 * modify and/ or redistribute the software under the terms of the CeCILL
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info". 
 * 
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability. 
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or 
 * data to be ensured and,  more generally, to use and operate it in the 
 * same conditions as regards security. 
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL license and that you accept its terms.
 * 
 */
package slib.sglib.algo.graph.extraction.rvf;

import java.util.Map;
import java.util.Set;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.utils.Direction;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public class DescendantEngine extends RVF_TAX {

    /**
     *
     * @param g
     */
    public DescendantEngine(G g) {
        super(g, Direction.IN, false);
    }

    /**
     *
     * @param g
     * @param acceptIncoherences true: accept to perform the process on cyclic graph... this can lead to highly incoherent results, special cares must be taken. DO NOT set this parameter to true if you don't understand the implications.
     */
    public DescendantEngine(G g, boolean acceptIncoherences) {
        super(g, Direction.IN, acceptIncoherences);
    }

    /**
     * Compute the set of exclusive descendants of a class. Exclusive process:
     * the focused vertex will NOT be included its the set of descendants.
     *
     * @param v the vertex of interest
     * @return the exclusive set of descendants of the concept (empty set if
     * any).
     */
    public Set<V> getDescendants(V v) {
        return getRV(v);
    }

    /**
     * Compute the set of exclusive descendants of all vertices contained in the
     * graph. Exclusive process: the focused vertex will NOT be included its the
     * set of ancestors.
     *
     * @return a map containing the exclusive set of descendants of each vertex
     * concept (empty set if any).
     * @throws SLIB_Ex_Critic
     */
    public Map<V, Set<V>> getAllDescendants() throws SLIB_Ex_Critic {
        return getAllRV();
    }
}
