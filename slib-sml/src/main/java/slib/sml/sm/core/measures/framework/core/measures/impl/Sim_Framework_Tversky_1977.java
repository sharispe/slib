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
 
 
package slib.sml.sm.core.measures.framework.core.measures.impl;

import slib.sml.sm.core.measures.framework.core.engine.GraphRepresentation;
import slib.sml.sm.core.measures.framework.core.engine.RepresentationOperators;
import slib.sml.sm.core.measures.framework.core.measures.Sim_Framework;
import slib.sml.sm.core.utils.SM_manager;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SGL_Ex_Critic;
import slib.utils.ex.SGL_Exception;

/**
 * ﻿﻿﻿1. Tversky A: Features of similarity. 
 * Psychological Review 1977, 84:327-352.
 * 
 * Ration model
 * 
 * @author Sebastien Harispe
 */
public class Sim_Framework_Tversky_1977 extends Sim_Framework{
	
	public static final String k_param_name = "k";
	private double k = 0.5; // do not set to 0 or 1 by default
	
	public Sim_Framework_Tversky_1977() {}
	
	public Sim_Framework_Tversky_1977(double k){
		this.k = k;
	}
	
	/**
	 * Depending on the configuration the measure can produce NaN due to the fact 
	 * that the denominator can be equals to 0 comparing two graphRepresentation A and B
	 * when the following conditions are encountered :
	 * 	 * <ul>
	 * <li> compared element are only annotated by the root
	 * </ul>
	 * or
	 * <ul>
	 * <li> k is set to 0 or 1
	 * <li> the commonalities between the two compared element produces 0
	 * <li> the the only subtraction to consider (according to k) produce 0
	 * </ul>
	 */
	public double compute(GraphRepresentation rep_a, GraphRepresentation rep_b, SM_manager c, RepresentationOperators operators, SMconf conf) throws SGL_Exception {
		
		if(! operators.asOperatorCommonalities())
			throw new SGL_Ex_Critic(this.getClass()+" requires operator commonality" +
					"to be defined which is not the case in "+operators.getClass());
		
		if(! operators.asOperatorDifference())
			throw new SGL_Ex_Critic(this.getClass()+" requires operator difference" +
					"to be defined which is not the case in "+operators.getClass());
		
		double commonalities = operators.commonalities(rep_a,rep_b,c);
		double diff_a_b = operators.subtraction(rep_a,rep_b,c);
		double diff_b_a = operators.subtraction(rep_b,rep_a,c);

		
		double den = commonalities + k * diff_a_b +  (1.-k) * diff_b_a  ;
		
		
		return (double) commonalities / den;
	}

	public double getK() {
		return k;
	}

	public void setK(double k) {
		this.k = k;
	}
	
	
	
	

}
