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
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.OperatorConf;
import slib.sml.sm.core.utils.SM_Engine;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.ResultStack;
import slib.utils.impl.SetUtils;

/**
 * {@link OperatorsSet_MAX_IC} object defines operators used to compare {@link GraphRepresentationAsSet} representation evaluating
 * the informativeness of terms. This class revisits {@link OperatorsSet_IC} evaluating :
 * <ul>
 * <li>informativeness : as the maximal IC of the classes contained in the set representation i.e. the IC
 * of the represented class</li>
 * <li>commonalities : as the maximal IC of the classes contained in the intersection</li>
 * <li>subtraction   : sub(a,b) as the difference between informativeness(a) - commonality(a,b)</li>
 * <li>difference   : as informativeness(a) + informativeness(b) - 2 commonality(a,b)</li>
 * </ul> 
 * 
 * The function used to defined class informativeness can be an IC (information Content) or another metric defined 
 * in the given configuration i.e. {@link OperatorConf} object specified during object creation. 
 * Note that the metric is assumed to monotonically decrease from the leaves to the root of the graph.
 * This class also defines rules to avoid NaN values if one the {@link GraphRepresentationAsSet} processed
 * only contains
 * @author Sebastien Harispe
 *
 */
public class OperatorsSet_MAX_IC extends RepresentationOperators{

	
	/**
	 * Create an {@link OperatorsSet_MAX_IC} object.
	 * see {@link #supportRepresentations(GraphRepresentation...)} to check the {@link GraphRepresentation}
	 * supported by the operators.
	 * The given configuration must contains an associated metric defining the method
	 * to use to compute {@link V} informativeness see {@link OperatorConf#ic} and {@link ICconf}
         * @param conf the configuration of the operator
         * @throws SLIB_Exception  
	 */
	public OperatorsSet_MAX_IC(OperatorConf conf) throws SLIB_Exception {
		super(conf);
		
		if(this.conf.ic == null)
			throw new SLIB_Exception("Please associate an IC configuration to operator "+conf.id+"  "+conf.flag);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The {@link OperatorsSet_MAX_IC} implementation evaluates commonalities the IC of represented
	 * classes MICA
         * 
         * @throws SLIB_Exception 
         */
	public double commonalities(GraphRepresentation rep_a, GraphRepresentation rep_b, SM_Engine manager) throws SLIB_Exception{
		
		Set<V> a = ((GraphRepresentationAsSet) rep_a).anc;
		Set<V> b = ((GraphRepresentationAsSet) rep_b).anc;
		
		ResultStack<V,Double> ics = manager.getIC_results(this.conf.ic);
		
		
		Set<V> inter = SetUtils.intersection(a, b);
		
		double max = 0;
		for(V v : inter){
			if(ics.get(v) > max)
				max = ics.get(v);
		}
		
		return max;
	}
	

	/**
	 * {@inheritDoc}
	 * 
	 * The {@link OperatorsSet_MAX_IC} implementation evaluates subtraction as informativeness 
	 * of the class represented by rep_a - commonality(rep_a,rep_b).
         * 
         * @throws SLIB_Exception 
         */
	public double subtraction(GraphRepresentation rep_a, GraphRepresentation rep_b, SM_Engine manager) throws SLIB_Exception {
		
		return informativeness(rep_a, manager) - commonalities(rep_a, rep_b, manager);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * The {@link OperatorsSet_MAX_IC} implementation evaluates difference as sum of the informativeness 
	 * of the compared representation -  2 * commonality(rep_a,rep_b).
         * 
         * @throws SLIB_Exception 
         */
	public double diff(GraphRepresentation rep_a, GraphRepresentation rep_b, SM_Engine manager) throws SLIB_Exception {
		return informativeness(rep_a, manager)+informativeness(rep_b, manager)- 2. *commonalities(rep_a, rep_b, manager);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * The {@link OperatorsSet_MAX_IC} implementation only supports graph representation as {@link GraphRepresentationAsSet}
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
	 * The {@link OperatorsSet_MAX_IC} implementation defines the informativeness as 
	 * the maximal IC of the classes'IC contained in the representation.
	 * Because the metric used to compute the IC must monotonically decrease from the leaves
	 * to the root informativeness(rep) must be equals to the IC of the class represented by rep.
         * 
         * @param rep 
         * @throws SLIB_Exception 
         */
	public double informativeness(GraphRepresentation rep,SM_Engine manager) throws SLIB_Exception {
		
		Set<V> a = ((GraphRepresentationAsSet) rep).anc;
		
		ResultStack<V,Double> ics = manager.getIC_results(this.conf.ic);
		
		// can be replaced by IC((V) rep.getResource())
		double max = 0;
		for(V v : a){
			if(ics.get(v) > max)
				max = ics.get(v);
		}
		return max;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Return true if the {@link GraphRepresentation} are supported see {@link #supportRepresentations(GraphRepresentation...)}
	 * and if compared representation not only contains the root of the graph
         * 
         * @throws SLIB_Exception 
         */
	public boolean validateRules(GraphRepresentation rep_a, GraphRepresentation rep_b, SM_Engine manager) throws SLIB_Exception {
		
		if(supportRepresentations(rep_a,rep_b)){
			
			Set<V> a = ((GraphRepresentationAsSet) rep_a).anc;
			Set<V> b = ((GraphRepresentationAsSet) rep_a).anc;
			
			V root = manager.getRoot();
			
			if(a.size() == 1 && a.iterator().next().equals(root))
				return false;
			
			if(b.size() == 1 && b.iterator().next().equals(root))
				return false;
			
			return true;
		}
		return false;
		
	}

	/**
	 * {@inheritDoc}
	 * 
	 * {@link OperatorsSet_MAX_IC} implements commonalities operator.	 
	 */
	public boolean asOperatorCommonalities() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * {@link OperatorsSet_MAX_IC} implements difference operator.	 
	 */
	public boolean asOperatorDifference() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * {@link OperatorsSet_MAX_IC} implements informativeness operator.	 
	 */
	public boolean asOperatorGRinformativness() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * {@link OperatorsSet_MAX_IC} implements subtraction operator.	 
	 */
	public boolean asOperatorSubstraction() {
		return true;
	}




}
