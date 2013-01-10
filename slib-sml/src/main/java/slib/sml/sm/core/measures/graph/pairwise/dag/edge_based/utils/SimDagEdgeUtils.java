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
 
 
package slib.sml.sm.core.measures.graph.pairwise.dag.edge_based.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import slib.sglib.algo.graph.shortest_path.Dijkstra;
import slib.sglib.model.graph.elements.V;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.ResultStack;
import slib.utils.impl.SetUtils;

/**
 *
 * @author seb
 */
public class SimDagEdgeUtils {



	/**
	 * Compute the most specific ancestor considering Pekar and Staab 2002 formula to minimize
	 * i.e. common ancestor minimizing score(c) = minDist_edge(A,c) + minDist_edge(B,c) + minDist_edge(ROOT,c) 
	 * 
	 * @see Pekar V, Staab S: Taxonomy learning: factoring the structure of a taxonomy into a semantic classification decision. 
	 * In COLING  ’02 Proceedings of the 19th international conference on Computational linguistics. 
	 * Association for Computational Linguistics; 2002, 2:1-7.
	 * 
         * @param root 
         * @param allSpA   	 	HashMap<V, Double> shortest path from A to all concepts
	 * @param allSpB		HashMap<V, Double> shortest path from B to all concepts
	 * @param ancestors_A	ArrayList<V> all ancestors of A
	 * @param ancestors_B	ArrayList<V> all ancestors of B
         * @param dijkstra 
         * @return V the vertex corresponding to the Most Specific Ancestors
	 */
	public static  V getMSA_pekar_staab(
			V root,
			Map<V, Double> allSpA,
			Map<V, Double> allSpB,
			Set<V> ancestors_A,
			Set<V> ancestors_B,
			Dijkstra dijkstra){

		Set<V> interSecAncestors = SetUtils.intersection(ancestors_A, ancestors_B);
		
//		System.out.println("intersec "+interSecAncestors);

		Map<V,Double> msaSet = new HashMap<V,Double>(); // eligible MSA + dist to root

		if(interSecAncestors.size() != 0){

			// Search MSA considering PK function 

			double scoremin = Double.MAX_VALUE;

			for (V r : interSecAncestors) {

				double sp_root = dijkstra.shortestPath(root, r);

				double score = allSpA.get(r) + allSpB.get(r) + sp_root;

//				System.out.println("\n\n"+r);
//				System.out.println("SP TO ROOT "+r+"\t"+sp_root);
//				System.out.println("A TO ROOT "+r+"\t"+allSpA.get(r));
//				System.out.println("B TO ROOT "+r+"\t"+allSpB.get(r));
//				System.out.println("V "+r+"\t"+score);

				if(scoremin > score){
					scoremin =  score;
					msaSet.clear();
					msaSet.put(r,sp_root);
				}
				else if(scoremin == score)
					msaSet.put(r,sp_root);
			}
		}
		
		V msa = null;
		double msalpr = 0;
		// we select the eligible msa with the longest shortest path to the root (lpr)
		// i.e. Optimistic implementation as more lpr is high, more the final score will be high
		
		for(Entry<V,Double> e : msaSet.entrySet()){
			
			if(e.getValue() > msalpr){
				msalpr = e.getValue();
				msa = e.getKey();
			}
		}
		return msa;
	}	
	
	/**
     *
     * @param interSecAncestors
     * @param maxDepths
     * @return
     * @throws SLIB_Exception
     */
    public static V searchMSA(Set<V> interSecAncestors,
			ResultStack<V,Integer> maxDepths) throws SLIB_Exception {
		
		
		V msa = null;
		
		for (V r : interSecAncestors) {
			
			if(msa == null || maxDepths.get(r) > maxDepths.get(msa))
				msa = r;
		}
		
		return msa;
	}



}
