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
package slib.sglib.algo.graph.accessor;

import java.util.HashSet;
import java.util.Set;
import org.openrdf.model.URI;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.E;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.type.VType;
import slib.sglib.model.graph.utils.Direction;
import slib.utils.impl.SetUtils;

/**
 *
 * @author Harispe Sébastien <harispe.sebastien@gmail.com>
 */
public class GraphAccessor {
    
    
    public static Set<V> getV_NoEdgeType(G g, URI edgeType, Direction dir) {
        return getV_NoEdgeType(g, SetUtils.buildSet(edgeType), dir);
    }

    public static Set<V> getV_NoEdgeType(G g, Set<URI> edgeTypes, Direction dir) {
        return getV_NoEdgeType(g, null, edgeTypes, dir);
    }

    public static Set<V> getV_NoEdgeType(G g, VType type, Set<URI> eTypes, Direction dir) {

        Set<V> valid = new HashSet<V>();

        Set<V> vSel = g.getV(type);

        if (dir == Direction.OUT || dir == Direction.BOTH) {

            for (V v : vSel) {
                Set<E> edgesSel = g.getE(v, Direction.OUT);

                boolean isValid = true;

                for (E e : edgesSel) {
                    if (eTypes == null
                            || eTypes.contains(e.getURI())) {

                        isValid = false;
                        break;
                    }
                }
                if (isValid) {
                    valid.add(v);
                }
            }
        }
        if (dir == Direction.IN || dir == Direction.BOTH) {

            for (V v : vSel) {
                Set<E> edges = g.getE(v, Direction.IN);

                boolean isValid = true;

                for (E e : edges) {
                    if (eTypes == null
                            || eTypes.contains(e.getURI())) {

                        isValid = false;
                        break;
                    }
                }
                if (isValid) {
                    valid.add(v);
                }
            }
        }
        return valid;
    }
    
}
