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
 * ﻿Zhou Z, Wang Y, Gu J: 
 * A New Model of Information Content for Semantic Similarity in WordNet.
 * In FGCNS  ’08 Proceedings of the 2008 Second International Conference 
 * on Future Generation Communication and Networking Symposia
 * Volume 03. IEEE Computer Society; 2008:85-89.
 * 
 * IC range : [0,1]
 * 
 * @author Sebastien Harispe
 * 
 */
public class ICi_zhou_2008 implements ICtopo{
	
	double k = 0.5;
	
	public ICi_zhou_2008(){}
	
	public ICi_zhou_2008(double k){
		this.k = k;
	}
	
	
	public ResultStack<V,Double> compute(	ResultStack<V,Integer> alldepths, 
										ResultStack<V,Long> allNbDescendants) throws SLIB_Exception{

		ResultStack<V,Double> results = new ResultStack<V,Double>(this.getClass().getSimpleName());
		
		double max_depth = alldepths.getMax() + 1;
		
		int nbHypo, depth;
		int nbConcepts = alldepths.size();
		
		double x,y,cur_ic;
		
		for ( V v:alldepths.keySet() ) {
			
			depth  = alldepths.get(v);
			nbHypo = allNbDescendants.get(v).intValue();
			
			
			x = k * (1. - Math.log( nbHypo) / Math.log( nbConcepts ) ) ;
			y = (1.-k) * ( Math.log( depth+1. ) / Math.log( max_depth ) );
			
			cur_ic =  x + y ;
			
			results.add(v, cur_ic);
		}

		return results;
	}

	public ResultStack<V,Double> compute(IC_Conf_Topo conf, SM_Engine manager)
			throws SLIB_Exception {
		
		if(conf.containsParam("k"))
			k = Double.parseDouble((String) conf.getParam("k"));
			
		return compute(manager.getMaxDepths(),manager.getAllNbDescendantsInc());
	}
}
