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
 
 
package slib.sml.sm.core.measures.others.groupwise.standalone.vector;

import java.util.Set;

import slib.sglib.model.graph.elements.V;
import slib.sml.sm.core.measures.Sim_Groupwise_Direct;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.ResultStack;

/**
 *
 * @author seb
 */
public class VectorSpaceModel implements Sim_Groupwise_Direct{

	/**
     *
     * @param setA
     * @param setB
     * @param rc
     * @param groupwiseconf
     * @return
     * @throws SLIB_Exception
     */
    public double sim(Set<V> setA, Set<V> setB, SM_Engine rc, SMconf groupwiseconf) throws SLIB_Exception {
		
		ResultStack<V,Double> v1 = rc.getVector(setA, groupwiseconf);
		ResultStack<V,Double> v2 = rc.getVector(setB, groupwiseconf);
		
		double num 	 = 0;
		double denum_w1 = 0;
		double denum_w2 = 0;
		
		double w1 = 0;
		double w2 = 0;
		
		for(V v : v1.getValues().keySet()){
			
			w1 = v1.get(v);
			w2 = v2.get(v);
			
			
			num   += w1 * w2; 
			denum_w1 += Math.pow(w1,2); 
			denum_w2 += Math.pow(w2,2);
		}
		
		double sim = 0;
		if(num != 0)
			sim = num / ( Math.sqrt(denum_w1) * Math.sqrt(denum_w2) );
		
		
		sim = Math.round(sim*10000)/10000.0d;
		
		return sim;
		
//		throw new UnsupportedOperationException();
	}
	
}
