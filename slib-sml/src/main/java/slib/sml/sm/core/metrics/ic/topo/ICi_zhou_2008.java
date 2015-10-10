/* 
 *  Copyright or © or Copr. Ecole des Mines d'Alès (2012-2014) 
 *  
 *  This software is a computer program whose purpose is to provide 
 *  several functionalities for the processing of semantic data 
 *  sources such as ontologies or text corpora.
 *  
 *  This software is governed by the CeCILL  license under French law and
 *  abiding by the rules of distribution of free software.  You can  use, 
 *  modify and/ or redistribute the software under the terms of the CeCILL
 *  license as circulated by CEA, CNRS and INRIA at the following URL
 *  "http://www.cecill.info". 
 * 
 *  As a counterpart to the access to the source code and  rights to copy,
 *  modify and redistribute granted by the license, users are provided only
 *  with a limited warranty  and the software's author,  the holder of the
 *  economic rights,  and the successive licensors  have only  limited
 *  liability. 

 *  In this respect, the user's attention is drawn to the risks associated
 *  with loading,  using,  modifying and/or developing or reproducing the
 *  software by the user in light of its specific status of free software,
 *  that may mean  that it is complicated to manipulate,  and  that  also
 *  therefore means  that it is reserved for developers  and  experienced
 *  professionals having in-depth computer knowledge. Users are therefore
 *  encouraged to load and test the software's suitability as regards their
 *  requirements in conditions enabling the security of their systems and/or 
 *  data to be ensured and,  more generally, to use and operate it in the 
 *  same conditions as regards security. 
 * 
 *  The fact that you are presently reading this means that you have had
 *  knowledge of the CeCILL license and that you accept its terms.
 */
package slib.sml.sm.core.metrics.ic.topo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.metrics.utils.LogBasedMetric;
import slib.sml.sm.core.utils.MathSML;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * ﻿Zhou Z, Wang Y, Gu J: A New Model of Information Content for Semantic
 * Similarity in WordNet. In FGCNS ’08 Proceedings of the 2008 Second
 * International Conference on Future Generation Communication and Networking
 * Symposia Volume 03. IEEE Computer Society; 2008:85-89.
 *
 * IC range : [0,1]
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 *
 */
public class ICi_zhou_2008 extends LogBasedMetric implements ICtopo {

    double k = 0.5;

    /**
     * Builder of an instance of IC computer. The parameter k is set to 0.5
     */
    public ICi_zhou_2008() {
    }

    /**
     * Builder of an instance of IC computer.
     *
     * @param k the value of the constant.
     */
    public ICi_zhou_2008(double k) {
        this.k = k;
    }

    /**
     * Compute the IC for each vertices expressed in the given result stack. The
     * two result stack are expected to contain the values for each processed
     * vertices.
     *
     * @param alldepths a result stack containing the depth for each vertices
     * @param allDescendantsIncs a map containing the set of inclusive
     * descendants for each vertices
     * @return a result stack containing the IC for each vertices.
     * @throws SLIB_Ex_Critic
     */
    public Map<URI, Double> compute(Map<URI, Integer> alldepths,
            Map<URI, Set<URI>> allDescendantsIncs) throws SLIB_Ex_Critic {

        Map<URI, Double> results = new HashMap<URI, Double>();

        double max_depth = Collections.max(alldepths.values()) + 1;

        int nbHypo, depth;
        int nbConcepts = alldepths.size();

        double x, y, cur_ic;

        for (URI v : alldepths.keySet()) {

            depth = alldepths.get(v);
            nbHypo = allDescendantsIncs.get(v).size();


            x = k * (1. - MathSML.log(nbHypo, getLogBase()) / MathSML.log(nbConcepts, getLogBase()));
            y = (1. - k) * (MathSML.log(depth + 1., getLogBase()) / MathSML.log(max_depth, getLogBase()));

            cur_ic = x + y;

            results.put(v, cur_ic);
        }

        return results;
    }

    @Override
    public Map<URI, Double> compute(IC_Conf_Topo conf, SM_Engine manager)
            throws SLIB_Ex_Critic {

        setLogBase(conf);

        if (conf.containsParam("k")) {
            k = Double.parseDouble((String) conf.getParam("k"));
        }

        return compute(manager.getMaxDepths(), manager.getAllDescendantsInc());
    }
}
