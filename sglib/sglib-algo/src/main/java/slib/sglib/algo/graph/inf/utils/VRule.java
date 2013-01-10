package slib.sglib.algo.graph.inf.utils;

import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.elements.type.VType;

/**
 *
 * @author seb
 */
public interface VRule {
	
		/**
     *
     * @param v
     * @return
     */
    public VType apply(V v);
}
