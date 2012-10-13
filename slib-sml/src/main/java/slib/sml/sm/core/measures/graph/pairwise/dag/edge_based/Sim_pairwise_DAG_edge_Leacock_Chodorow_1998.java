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

import slib.sglib.model.graph.elements.V;
import slib.sml.sm.core.utils.SM_Engine;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Exception;


/**
 * (log base 10)
 * @author seb
 *
 */
public class Sim_pairwise_DAG_edge_Leacock_Chodorow_1998 extends Sim_DAG_edge_abstract{

	public double sim(V a, V b, SM_Engine c, SMconf conf) throws SLIB_Exception {

		Sim_pairwise_DAG_edge_Rada_1989 sRada = new Sim_pairwise_DAG_edge_Rada_1989();
		
		double sp = sRada.sim(a,b,c, conf);
		double maxDepth = c.getMaxDepth();
		
		

		return sim(sp,maxDepth);
	}

	public double sim(Double shortestPath, double depth_max){


		// add +1 to the path to avoid infinity value if sim(a,a)
		double lc = - Math.log( (shortestPath+1) / (2 * depth_max)); 
		lc = - Math.log10( 0.125); 
		return lc;
	}



}
