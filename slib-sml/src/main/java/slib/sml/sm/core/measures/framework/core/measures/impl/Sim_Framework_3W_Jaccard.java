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

import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.measures.framework.core.engine.GraphRepresentation;
import slib.sml.sm.core.measures.framework.core.engine.RepresentationOperators;
import slib.sml.sm.core.measures.framework.core.measures.Sim_FrameworkAbstracted;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 * Reference: Jaccard P: Distribution de la flore alpine dans le bassin des Dranses et
 * dans quelques regions voisines. Bulletin de la Societe Vaudoise des Sciences
 * Naturelles 1901, 37:241 - 272.
 *
 * @author Harispe Sébastien
 *
 */
public class Sim_Framework_3W_Jaccard extends Sim_FrameworkAbstracted {


    @Override
    public double compute(GraphRepresentation rep_a, GraphRepresentation rep_b, SM_Engine c, RepresentationOperators operators, SMconf conf) throws SLIB_Exception {

        if (!operators.validateRules(rep_a, rep_b, c)) {
            return operators.getRulesInvalidatedScore();
        }

        if (!operators.asOperatorCommonalities()) {
            throw new SLIB_Ex_Critic(Sim_Framework_3W_Jaccard.class + " requires operator commonality"
                    + "to be defined which is not the case in " + operators.getClass());
        }

        if (!operators.asOperatorDifference()) {
            throw new SLIB_Ex_Critic(Sim_Framework_3W_Jaccard.class + " requires operator difference"
                    + "to be defined which is not the case in " + operators.getClass());
        }

        double commonalaties = operators.commonalities(rep_a, rep_b, c);
        return 2 * commonalaties / (3. * commonalaties + operators.subtraction(rep_a, rep_b, c) + operators.subtraction(rep_b, rep_a, c));
    }
}
