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
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import slib.sglib.algo.traversal.classical.BFS;
import slib.sglib.model.graph.G;
import slib.sglib.model.graph.elements.V;
import slib.sglib.model.graph.utils.WalkConstraints;

/**
 * Reachable Vertex Finder (RVF) objects can be used to retrieve the vertices reachable from a particular
 * vertex of a graph considering particular constraints e.g. type of relationships (i.e. set of predicate URI)
 * particular vertex restriction applied to the walk.
 * 
 * @author Sebastien Harispe
 */
public class RVF{
	
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	protected WalkConstraints wc;
	protected G g;
	
	 /**
	  *  Reachable vertex Finder
	  * @param g	
	  * @param eType authorized edge types 
	  * @param vType authorized vertex types
	  */
	public RVF(G g, WalkConstraints wc){
		this.g = g;
		this.wc = wc;
	}
	
	
	
	/**
	 * Retrieve the set of reachable vertices RV from a specified vertex taking into 
	 * account the type of relationships associated to the current instance.
	 * Note that if the graph is acyclic the set of vertices returned respect the topological ordering defined
	 * by the type of relationships considered as a Bread First Search is performed.
	 *  
	 * @param v the focus vertex
	 * @return the set of vertices encountered during the traversal.
	 */
	public Set<V> getRV(V v){
		
		List<V> rv = new ArrayList <V>();
		
		BFS it = new BFS(g, v, wc);
		
		while (it.hasNext()) {
			
			V next = it.next();
			rv.add(next);
		}
		return new HashSet<V>(rv);
	}
	

	/**
	 * Compute the set of reachable vertices for each vertices composing
	 * the graph associated to the object considering the edge types loaded.
	 * @FIXME optimize
	 * 
	 * @return an HashMap key V value the set of vertices reachable from the key Set<V>
	 */
	public Map<V, Set<V>> getRV(Set<V> queryVertices){
		
		logger.debug("Get All reachable vertices : start");
		
		Map<V, Set<V>> allVertices = new HashMap<V, Set<V>>();
		
		for(V v : queryVertices)
			allVertices.put(v, getRV(v));
	
		logger.debug("Get All reachable vertices : end");
		return allVertices;
	}
	
	/**
	 * Return the number of vertices reachable from all vertices composing 
	 * the graph associated to the instance.
	 * 
	 * @return an HashMap key V value the size of the set of vertices reachable from the key as Integer
	 */
	public Map<V, Integer> getRVnb(Set<V> queryVertices){
		
		Map<V, Set<V>> r = getRV(queryVertices);
		Map<V, Integer> results = new HashMap<V, Integer>(r.size());
		
		for(Entry<V,Set<V>> entry : r.entrySet())
			results.put(entry.getKey(), entry.getValue().size());
		
		return results;
	}
	

}
