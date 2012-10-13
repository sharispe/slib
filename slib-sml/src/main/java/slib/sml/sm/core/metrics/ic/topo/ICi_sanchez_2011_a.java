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
 
 
package slib.sml.sm.core.metrics.ic.topo;

import slib.sglib.model.graph.elements.V;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.utils.SM_Engine;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.ResultStack;


/**
 * 
 * ﻿Sanchez D, Batet M, Isern D: 
 * Ontology-based information content computation. 
 * Knowledge-Based Systems 2011, 24:297-303.
 * 
 * formula  equation 10 p 300
 * 
 * IC inner expression range : ]0,1]
 * IC value : [0,...[
 * 
 * IC is normalize considering spirit formulated in given by Faria and al in order to produce results [0,1].
 * ﻿Faria D, Pesquita C, Couto FM, Falcão A: Proteinon: A web tool for protein semantic similarity. 2007.
 *  
 * @author Sebastien Harispe
 */
public class ICi_sanchez_2011_a implements ICtopo{
	
	// TODO include current leaf into Reachable leaves
	public ResultStack<V,Double> compute(ResultStack<V,Double> allNbOfReachableLeaves, 
			ResultStack<V,Double> allNbAncestors) throws SLIB_Exception{

		ResultStack<V,Double> results = new ResultStack<V,Double>(this.getClass().getSimpleName());
		
		double max_leaves = allNbOfReachableLeaves.getMax();
		
		int nbLeaves, nbAncestors;
		double x,y, cur_ic, cur_ic_norm;
		
		y = (double) max_leaves +1;
		
		
		
		for ( V v:allNbAncestors.keySet() ) {
			
			nbAncestors = allNbAncestors.get(v).intValue();
			nbLeaves	= allNbOfReachableLeaves.get(v).intValue();
			
			x = (double) nbLeaves/nbAncestors + 1;
			
//			System.out.println(nbAncestors);
//			System.out.println(v+" "+x);
			
			cur_ic = - Math.log( ( x )  / ( y ) );
			cur_ic_norm = cur_ic / Math.log(  y  );
			
//			System.out.println(v+"  "+cur_ic+"  "+cur_ic_norm);
			
			results.add(v, cur_ic_norm);
		}

		return results;
	}
	
	public ResultStack<V,Double> compute(IC_Conf_Topo conf, SM_Engine manager)
			throws SLIB_Exception {
		
		ResultStack<V,Double> allNbAncestors 		 	= manager.getAllNbAncestors();
		ResultStack<V,Double> allNbReachableLeaves    = manager.getAllNbReachableLeaves();
		return compute(allNbReachableLeaves,allNbAncestors);
	}
}
