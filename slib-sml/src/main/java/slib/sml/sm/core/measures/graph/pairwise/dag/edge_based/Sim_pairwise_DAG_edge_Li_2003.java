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
 
 
package slib.sml.sm.core.measures.graph.pairwise.dag.edge_based;


import java.util.Set;

import slib.sglib.model.graph.elements.V;
import slib.sml.sm.core.measures.graph.pairwise.dag.edge_based.utils.SimDagEdgeUtils;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.ResultStack;
import slib.utils.impl.SetUtils;


/**
 * 
 * ﻿﻿Li Y, Bandar ZA, McLean D: An approach for measuring semantic similarity between words using multiple information sources. 
 * IEEE Transactions on Knowledge and Data Engineering 2003, 15:871-882.
 *
 * TODO check LCA restriction
 */
public class Sim_pairwise_DAG_edge_Li_2003 extends Sim_DAG_edge_abstract{


	// @see publication
	double alpha = 0.2;
	double beta  = 0.6;

	/**
     *
     * @param a
     * @param b
     * @param c
     * @param conf
     * @return
     * @throws SLIB_Exception
     */
    public double sim(V a, V b, SM_Engine c, SMconf conf) throws SLIB_Exception {

		double sp_AtoB 				  = c.getShortestPath(a, b);
		Set<V> ancestors_A 	  = c.getAncestorsInc(a);
		Set<V> ancestors_B 	  = c.getAncestorsInc(b);
		ResultStack<V,Integer> maxDepths = c.getMaxDepths();

		return sim(sp_AtoB, ancestors_A, ancestors_B, maxDepths);
	}

	/**
	 * Revenir sur la recherche du msa de Li
	 * Pour Li le msa est le concept de plus faible profondeur 
	 * qui appartient au chemin le plus court entre les deux concepts 
	 * passant par un ancetre commun des deux concepts
	 * 
	 * Alpha in [0,1]   (best : 0.2)
	 * Beta in  ]0,1]	(best : 0.6)
         * @param sp_AtoB 
         * @param ancestors_A 
         * @param ancestors_B 
         * @param maxDepths 
         * @return
         * @throws SLIB_Exception  
	 */
	public double sim(  double sp_AtoB, 
			Set<V> ancestors_A,
			Set<V> ancestors_B,
			ResultStack<V,Integer> maxDepths) throws SLIB_Exception {

		double sim = 0;


		Set<V> interSecAncestors = SetUtils.intersection(ancestors_A, ancestors_B);

		if(interSecAncestors.size() != 0){

			V msa = SimDagEdgeUtils.searchMSA(interSecAncestors,maxDepths);

			int h = maxDepths.get(msa);

			double f1 =   Math.exp(-alpha*sp_AtoB); 
			double f2 = ( Math.exp(beta*h) - Math.exp(-beta*h) ) / ( Math.exp(beta*h) + Math.exp(-beta*h) );

			sim = f1 * f2;

		}

		return sim;
	}



}
