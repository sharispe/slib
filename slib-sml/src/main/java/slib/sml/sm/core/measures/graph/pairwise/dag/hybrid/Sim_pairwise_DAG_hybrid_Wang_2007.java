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
 
 
package slib.sml.sm.core.measures.graph.pairwise.dag.hybrid;

import java.util.Map;
import java.util.Set;

import slib.sglib.model.graph.elements.V;
import slib.sml.sm.core.measures.graph.pairwise.dag.edge_based.Sim_DAG_edge_abstract;
import slib.sml.sm.core.utils.SM_Engine;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.impl.SetUtils;

/**
 * ﻿Wang JZ, Du Z, Payattakool R, Yu PS, Chen C-F: A new method to measure the semantic similarity of GO terms. 
 * Bioinformatics (Oxford, England) 2007, 23:1274-81.
 * 
 * @author Sébastien Harispe
 *
 */
public class Sim_pairwise_DAG_hybrid_Wang_2007 extends Sim_DAG_edge_abstract{


	public double sim(V a, V b, SM_Engine c, SMconf conf) {

		Map<V, Double> sc_A = c.computeSemanticContribution(a);
		Map<V, Double> sc_B = c.computeSemanticContribution(b);


		Set<V> ancestors_A = c.getAncestorsInc(a);
		Set<V> ancestors_B = c.getAncestorsInc(b);

		double svA = c.computeSV_Wang_2007(a);
		double svB = c.computeSV_Wang_2007(b);

		return sim(a,b,sc_A,sc_B,svA,svB,ancestors_A,ancestors_B);
	}


	public double sim(	V cA, 
			V cB, 
			Map<V, Double> sc_A,
			Map<V, Double> sc_B, 
			double svA, 
			double svB,
			Set<V> ancA,
			Set<V> ancB){



		double sim = 0;

		Set<V> interSecAncestors = SetUtils.intersection(ancA, ancB);

		double num = 0;

		for (V r: interSecAncestors)
			num += sc_A.get(r) + sc_B.get(r);

		double denum = svA+svB;

		sim = num / denum;
		return sim;
	}



}
