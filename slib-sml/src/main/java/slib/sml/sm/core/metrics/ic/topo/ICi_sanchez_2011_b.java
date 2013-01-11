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
import slib.sml.sm.core.metrics.utils.LogBasedMetric;
import slib.sml.sm.core.utils.MathSML;
import slib.sml.sm.core.engine.SM_Engine;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.ResultStack;

/**
 *
 * @author seb ﻿1. Sanchez D, Batet M, Isern D: Ontology-based information
 * content computation. Knowledge-Based Systems 2011, 24:297-303.
 *
 * formula equation 9 p 299
 *
 * IC inner expression range : ]0,1] IC value : [0,...[
 *
 * IC is normalize considering spirit formulated in Faria and al in order to
 * produce results [0,1]. ﻿Faria D, Pesquita C, Couto FM, Falcão A: Proteinon: A
 * web tool for protein semantic similarity. 2007.
 */
public class ICi_sanchez_2011_b extends LogBasedMetric implements ICtopo {

    // TODO include current leaf
    /**
     *
     * @param allNbOfReachableLeaves
     * @return
     * @throws SLIB_Ex_Critic
     */
    public ResultStack<V, Double> compute(ResultStack<V, Double> allNbOfReachableLeaves) throws SLIB_Ex_Critic {

        ResultStack<V, Double> results = new ResultStack<V, Double>(this.getClass().getSimpleName());

        double max_leaves = allNbOfReachableLeaves.getMax();

        double nbLeaves;
        double x, cur_ic, cur_ic_norm;

        double y = (double) max_leaves + 1;

        for (V v : allNbOfReachableLeaves.getValues().keySet()) {

            nbLeaves = allNbOfReachableLeaves.get(v);

            x = (double) nbLeaves + 1;

            cur_ic = -MathSML.log(x / y, getLogBase());

            cur_ic_norm = cur_ic / MathSML.log(y, getLogBase());

            results.add(v, cur_ic_norm);
        }

        return results;
    }

    @Override
    public ResultStack<V, Double> compute(IC_Conf_Topo conf, SM_Engine manager)
            throws SLIB_Ex_Critic {

        setLogBase(conf);

        return compute(manager.getAllNbReachableLeaves());
    }
}
