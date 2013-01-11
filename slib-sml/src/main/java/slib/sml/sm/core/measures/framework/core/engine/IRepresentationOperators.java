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
 
 
package slib.sml.sm.core.measures.framework.core.engine;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.utils.OperatorConf;
import slib.utils.ex.SLIB_Exception;

/**
 * Interface which defines the methods required to define operators
 * for particular graph representations.
 * 
 * @author Sebastien Harispe
 *
 */
public interface IRepresentationOperators{

	/**
	 * Evaluate if the given {@link GraphRepresentation} are supported by the operators. 
	 * 
	 * @param reps the {@link GraphRepresentation} to evaluate
	 * @return true if the operators can be used for all given representations
	 */
	public boolean supportRepresentations(GraphRepresentation... reps);
	
	/**
	 * Evaluate the commonalities between two given graph representations.
	 * Assigns a double value to the evaluated commonality.
	 * As an example for two set representations A and B this operator corresponds to the intersection(A,B)
	 * 
	 * @param rep_a a {@link GraphRepresentation}
	 * @param rep_b a {@link GraphRepresentation}
	 * @param manager the {@link SM_Engine} used to process the graph
	 * @return a double value corresponding to the commonality between the {@link GraphRepresentation}
         * @throws SLIB_Exception 
	 */
	public double commonalities(GraphRepresentation rep_a, GraphRepresentation rep_b, SM_Engine manager) throws SLIB_Exception;
	
	/**
	 * Evaluate the subtraction between two given graph representations.
	 * Assigns a double value to the evaluated subtraction.
	 * As an example for two set representations A and B, this operator corresponds to the A - intersection(A,B)
	 * 
	 * @param rep_a a {@link GraphRepresentation}
	 * @param rep_b a {@link GraphRepresentation}
	 * @param manager the {@link SM_Engine} used to process the graph
	 * @return a double value corresponding to the subtraction between the {@link GraphRepresentation}
         * @throws SLIB_Exception 
	 */
	public double subtraction(GraphRepresentation rep_a, GraphRepresentation rep_b, SM_Engine manager) throws SLIB_Exception;
	
	/**
	 * Evaluate the difference between two given graph representations.
	 * Assigns a double value to the evaluated difference.
	 * As an example for two set representations A and B, this operator corresponds to the symmetric difference i.e. A + B - 2 intersection(A,B)
	 * 
	 * @param rep_a a {@link GraphRepresentation}
	 * @param rep_b a {@link GraphRepresentation}
	 * @param manager the {@link SM_Engine} used to process the graph
	 * @return a double value corresponding to the difference between the {@link GraphRepresentation}
         * @throws SLIB_Exception 
	 */
	public double diff(GraphRepresentation rep_a, GraphRepresentation rep_b, SM_Engine manager) throws SLIB_Exception;
	
	/**
	 * Evaluate the informativeness of a given graph representation i.e. the amount of information contained be the graph representation
	 * Assigns a double value to the evaluated informativeness.
	 * As an example for a set representation A, this operator corresponds to |A|, the size of the set
	 * 
         * @param rep 
	 * @param manager the {@link SM_Engine} used to process the graph
	 * @return a double value corresponding to the informativeness of the {@link GraphRepresentation}
         * @throws SLIB_Exception 
	 */
	public double informativeness(GraphRepresentation rep, SM_Engine manager) throws SLIB_Exception;

	/**
	 * Return a boolean defining if the operator to evaluate commonalities is defined.
	 * @return true if the operator to evaluate commonalities is defined
	 * @see #commonalities(GraphRepresentation, GraphRepresentation, SM_manager);
	 */
	public boolean asOperatorCommonalities();
	
	/**
	 * Return a boolean defining if the operator to evaluate subtraction is defined.
	 * @return true if the operator to evaluate subtraction is defined
	 * @see #subtraction(GraphRepresentation, GraphRepresentation, SM_Engine)
	 */
	public boolean asOperatorSubstraction();
	
	/**
	 * Return a boolean defining if the operator to evaluate difference is defined.
	 * @return true if the operator to evaluate difference is defined
	 * @see #diff(GraphRepresentation, GraphRepresentation, SM_Engine)
	 */
	public boolean asOperatorDifference();
	
	/**
	 * Return a boolean defining if the operator to evaluate informativeness is defined.
	 * @return true if the operator to evaluate informativeness of a graph representation is defined
	 * @see IRepresentationOperators#informativeness(GraphRepresentation, SM_Engine)
	 */
	public boolean asOperatorGRinformativness();
	
	/**
	 * Check if the rules applying for the operators are respected for the given graph representations
	 * The rules are generally used to check if the graph representations can be processed by the operators engine
	 * see {@link #supportRepresentations(GraphRepresentation...)}. Conditions can also be checked to avoid
	 * abnormal computation such as 0 division.
	 * As an example, when classes are represented by there set of inclusive ancestors
	 * and the informativeness is evaluated as the sum of there information content the commonality between two graph representations
	 * representing the root of the process graph will led to 0 as the differences. 
	 * In this case a measure such as Dice coefficient will generate infinite values.<br/>
	 * 
	 * @param rep_a a {@link GraphRepresentation}
	 * @param rep_b a {@link GraphRepresentation}
	 * @param manager the {@link SM_Engine} used to process the graph
	 * @return true if the rules are validated
         * @throws SLIB_Exception 
         * @see {@link #subtraction(GraphRepresentation, GraphRepresentation, SM_Engine)}
	 */
	public boolean validateRules(GraphRepresentation rep_a, GraphRepresentation rep_b, SM_Engine manager) throws SLIB_Exception;
	
	/**
	 * Return the score to affect to comparison for which operators rules are not validated
	 * @return a default score i.e. 0
	 * @see IRepresentationOperators#validateRules(GraphRepresentation, GraphRepresentation, SM_Engine)
	 */
	public double  getRulesInvalidatedScore();
	
	/**
	 * Get the configuration associated to the operators
	 * @return {@link OperatorConf} the configuration
	 */
	public OperatorConf getConf();

	// Add support commonalities which is evaluate before using measure requiring the functionality
}
