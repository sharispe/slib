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
package slib.sml.sm.core.metrics.ic.topo;

import slib.sglib.model.graph.elements.V;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.utils.MathSML;
import slib.sml.sm.core.utils.SMParams;
import slib.sml.sm.core.utils.SM_Engine;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.ResultStack;

/**
 * ﻿1. Seco N, Veale T, Hayes J: An Intrinsic Information Content Metric for
 * Semantic Similarity in WordNet. In 16th European Conference on Artificial
 * Intelligence. IOS Press; 2004, 16:1-5.
 *
 * IC range : [0,1]
 *
 * @author Sebastien Harispe
 */
public class ICi_seco_2004 implements ICtopo {
    
    Double logbase = null;

    public ResultStack<V, Double> compute(ResultStack<V, Long> allNbOfDescendants) throws SLIB_Exception {

        ResultStack<V, Double> results = new ResultStack<V, Double>(this.getClass().getSimpleName());


        double x, cur_ic, nbDesc;

        double setSize = allNbOfDescendants.size(); // i.e. number of concepts in the processed graph

        for (V v : allNbOfDescendants.keySet()) {

            nbDesc = allNbOfDescendants.get(v).doubleValue();

            // The formulation of Seco do not consider inclusive descendants but add + 1 to correct
            // the nominator and thus obtain log(0) for a leaf.
            // In our case the beahaviour is the same as in the original formulation

            x = MathSML.log(nbDesc, logbase) / MathSML.log(setSize, logbase);
            cur_ic = 1. - x;

            if (Double.isNaN(cur_ic) || Double.isInfinite(cur_ic)) {
                throw new SLIB_Ex_Critic(
                        "Incoherency found in IC " + this.getClass() + "\n"
                        + "Log base       " + logbase +"\n"
                        + "Log nbDesc     " + MathSML.log(nbDesc, logbase)  + "\n"
                        + "Log Set size   " + MathSML.log(setSize, logbase) + "\n"
                        + "IC of vertex " + v + " is set to " + x + "\n"
                        + "Number of Descendants: " + nbDesc + "\n"
                        + "SetSize: " + setSize + "\n"
                        + "");
            }
            results.add(v, cur_ic);
        }

        return results;
    }

    @Override
    public ResultStack<V, Double> compute(IC_Conf_Topo conf, SM_Engine manager) throws SLIB_Exception {

        if (conf.containsParam(SMParams.LOG_BASE.toString())) {
            logbase = conf.getParamAsDouble(SMParams.LOG_BASE.toString());
        }

        // The formulation of Seco do not consider inclusive descendants but add + 1 to correct
        return compute(manager.getAllNbDescendantsInc());
    }
}
