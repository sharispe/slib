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
import slib.sml.sm.core.measures.framework.core.engine.IGraphRepresentation;
import slib.sml.sm.core.measures.framework.core.engine.RepresentationOperators;
import slib.sml.sm.core.utils.OperatorConf;
import slib.sml.sm.core.utils.SM_manager;
import slib.utils.ex.SGL_Exception;
import slib.utils.impl.SetUtils;


public class OperatorsSet extends RepresentationOperators{

	/**
	 * Create an {@link OperatorsSet} object.
	 * see {@link #supportRepresentations(GraphRepresentation...)} to check the {@link GraphRepresentation}
	 * supported by the operators.
	 * 
	 * @param conf the configuration of the operator
	 */
	public OperatorsSet(OperatorConf conf) {
		super(conf);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The {@link OperatorsSet} implementation evaluates commonalities as the cardinality of the set intersection.
	 */
	public double commonalities(GraphRepresentation rep_a, GraphRepresentation rep_b, SM_manager manager) throws SGL_Exception{
		
		Set<V> a = ((GraphRepresentationAsSet) rep_a).anc;
		Set<V> b = ((GraphRepresentationAsSet) rep_b).anc;
	
		return SetUtils.intersection(a, b).size();
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * The {@link OperatorsSet} implementation evaluates subtraction 
	 * as the cardinality of the first given {@link GraphRepresentation} 
	 * minus the cardinality of the intersection of the two.
	 */
	public double subtraction(GraphRepresentation rep_a,
			GraphRepresentation rep_b, SM_manager manager)
			throws SGL_Exception {
		
		Set<V> a = ((GraphRepresentationAsSet) rep_a).anc;
		Set<V> b = ((GraphRepresentationAsSet) rep_b).anc;
		
		return a.size() - SetUtils.intersection(a, b).size();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The {@link OperatorsSet} implementation evaluates difference 
	 * as the summed cardinality of the compared {@link GraphRepresentation} 
	 * minus twice the cardinality of their intersection.
	 */
	public double diff(GraphRepresentation rep_a, GraphRepresentation rep_b, SM_manager manager) throws SGL_Exception {
		
		Set<V> a = ((GraphRepresentationAsSet) rep_a).anc;
		Set<V> b = ((GraphRepresentationAsSet) rep_b).anc;
		
		return a.size() + b.size() - 2 * SetUtils.intersection(a, b).size();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The {@link OperatorsSet} implementation only supports graph representation as {@link GraphRepresentationAsSet}
	 */
	public boolean supportRepresentations(GraphRepresentation... reps){
		for (IGraphRepresentation r : reps){
		
			if(!(r instanceof GraphRepresentationAsSet))
				return false;
		}
		return true;
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * The {@link OperatorsSet} implementation defines the informativeness as the cardinality of the set of vertices
	 */
	public double informativeness(GraphRepresentation rep, SM_manager manager) throws SGL_Exception {
		return ((GraphRepresentationAsSet) rep).anc.size();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * {@link OperatorsSet} implements commonalities operator.	 
	 */
	public boolean asOperatorCommonalities() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * {@link OperatorsSet} implements difference operator.	 
	 */
	public boolean asOperatorDifference() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * {@link OperatorsSet} implements informativeness operator.	 
	 */
	public boolean asOperatorGRinformativness() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * {@link OperatorsSet} implements subtraction operator.	 
	 */
	public boolean asOperatorSubstraction() {
		return true;
	}

	/**
	 * 
	 * 
	 * Only check if the given representations are supported.	 
	 * 
	 * <br/><br/> See interface doc below <br/> {@inheritDoc}
	 */
	public boolean validateRules(GraphRepresentation a,
			GraphRepresentation b, SM_manager manager) throws SGL_Exception {
		return supportRepresentations(a,b);
		
	}



}
