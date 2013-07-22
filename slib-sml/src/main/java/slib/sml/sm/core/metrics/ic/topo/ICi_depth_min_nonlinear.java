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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.openrdf.model.URI;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.metrics.utils.LogBasedMetric;
import slib.sml.sm.core.utils.MathSML;
import slib.sml.sm.core.engine.SM_Engine;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * @author Sebastien Harispe
 *
 */
public class ICi_depth_min_nonlinear extends LogBasedMetric implements ICtopo {

    /**
     *
     * @param alldepths
     * @return the IC of all URIs specified in the given map.s
     * @throws SLIB_Ex_Critic
     */
    public Map<URI, Double> compute(Map<URI, Integer> alldepths) throws SLIB_Ex_Critic {

        Map<URI, Double> results = new HashMap<URI, Double>();

        double max_depth = Collections.max(alldepths.values()) + 1;

        int depth;

        double cur_ic;

        for (URI v : alldepths.keySet()) {

            depth = alldepths.get(v);

            cur_ic = MathSML.log(depth + 1., getLogBase()) / MathSML.log(max_depth, getLogBase());

            results.put(v, cur_ic);
        }

        return results;
    }

    @Override
    public Map<URI, Double> compute(IC_Conf_Topo conf, SM_Engine manager)
            throws SLIB_Ex_Critic {

        setLogBase(conf);

        return compute(manager.getMinDepths());
    }
}
