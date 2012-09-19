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
 
 
package slib.sml.sm.core.measures.graph.pairwise.dag.node_based;

import slib.sglib.model.graph.elements.V;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SM_manager;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SGL_Ex_Critic;
import slib.utils.ex.SGL_Exception;

public class Sim_pairwise_DAG_node_Schlicker_Jaccard_SimRel implements Sim_DAG_node_abstract{
	
	
	public double sim(V a, V b, SM_manager c, SMconf conf) throws SGL_Exception {
		
		double ic_a = c.getIC(conf.getICconf(),a);
		double ic_b = c.getIC(conf.getICconf(),b);
		double ic_MICA = c.getIC_MICA(conf.getICconf(),a,b);
		
		ICconf confic = (ICconf) conf.getParam(Sim_pairwise_DAG_node_Constants.IC_PROB);
		
		if(confic == null)
			throw new SGL_Ex_Critic("Measure "+this.getClass().getSimpleName()+" requires a parameter: "+Sim_pairwise_DAG_node_Constants.IC_PROB);
		
		double p_MICA  = c.getP_MICA( confic, a,b);
		
		return sim(ic_a, ic_b, ic_MICA, p_MICA);
	}

	
	
	public double sim(double ic_a, double ic_b, double ic_mica, double p_mica) {
		
		Sim_pairwise_DAG_node_Jaccard_IC simJaccard = new Sim_pairwise_DAG_node_Jaccard_IC();
		
		double sim = simJaccard.sim(ic_a, ic_b, ic_mica);
		
		return sim * (1. - p_mica);
	}

	
}
