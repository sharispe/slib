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
 
 
package slib.sml.sm.core.measures.framework.impl.set;

import java.util.Set;

import slib.sglib.model.graph.elements.V;
import slib.sml.sm.core.measures.framework.core.engine.GraphRepresentation;
import slib.sml.sm.core.utils.OperatorConf;
import slib.sml.sm.core.utils.SM_Engine;
import slib.utils.ex.SLIB_Exception;

/**
 * {@link OperatorsSet_ICA_Grasm_IC} extends {@link OperatorsSet_IC} in order to
 * redefine commonalities evaluation. The commonality between two classes is evaluated
 * as the sum of their disjoint common ancestors (dca) {@link GraphRepresentationAsSet} 
 * informativeness divided by the number of dca.
 * 
 * @author Sebastien Harispe
 *
 */
public class OperatorsSet_ICA_Grasm_IC extends OperatorsSet_IC{


	public OperatorsSet_ICA_Grasm_IC(OperatorConf conf) throws SLIB_Exception {
		super(conf);
	}


	/**
	 *  {@inheritDoc}
	 * 
	 * {@link OperatorsSet_ICA_Grasm_IC} extends {@link OperatorsSet_IC} in order to
	 * redefine commonalities evaluation. The commonality between two classes is evaluated
	 * as the sum of their disjoint common ancestors (dca) {@link GraphRepresentationAsSet} 
	 * informativeness divided by the number of dca.
	 */
	@Override
	public double commonalities(GraphRepresentation rep_a, GraphRepresentation rep_b, SM_Engine manager) throws SLIB_Exception{

		V class_a = (V) manager.getGraph().getV(rep_a.getResourceValue());
		V class_b = (V) manager.getGraph().getV(rep_b.getResourceValue());

		Set<V> set_dca = manager.getDisjointCommonAncestors(class_a, class_b);

		double dca_informativness = 0;
		for(V dca : set_dca){

			GraphRepresentationAsSet dca_rep = new GraphRepresentationAsSet(dca, manager);
			dca_informativness += informativeness(dca_rep, manager);
		}

		return (double) dca_informativness/set_dca.size();
	}

}
