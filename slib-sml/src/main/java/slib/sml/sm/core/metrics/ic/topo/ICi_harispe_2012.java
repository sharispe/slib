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
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.metrics.utils.LogBasedMetric;
import slib.sml.sm.core.utils.MathSML;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * Modification of Sanchez et al. IC in order to authorize various non
 * uniformity of ICs among the leafs
 *
 * IC(u) = -log( ( leavesInc(u) / ancsInc(u) ) / (MAX_LEAVES))
 *
 * with leaves(u) a function computing the set of reachable leaves from a
 * concept considering (in opposition to the Sanchez et al. definition) that the
 * function leavesInc(c) = |{c}| when c is a root. MAX_LEAVES the number of
 * leaves in the graph i.e. the number of leaves reachable from the root
 * ancsInc, the number of inclusive ancestors of a node
 *
 * Original definition IC(u) = -log( ( leaves(u) / ancsInc(u) +1 ) / (MAX_LEAVES
 * + 1))
 *
 *
 * See Sanchez et al for original definition ﻿Sanchez D, Batet M, Isern D:
 * Ontology-based information content computation. Knowledge-Based Systems 2011,
 * 24:297-303.
 *
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class ICi_harispe_2012 extends LogBasedMetric implements ICtopo {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     *
     * @param allNbOfReachableLeaves
     * @param allNbAncestors
     * @return the IC of all URIs specified in the given maps.
     * @throws SLIB_Ex_Critic
     */
    public Map<URI, Double> compute(Map<URI, Integer> allNbOfReachableLeaves,
            Map<URI, Integer> allNbAncestors) throws SLIB_Ex_Critic {

        Map<URI, Double> results = new HashMap<URI, Double>();

        double max_leaves = Collections.max(allNbOfReachableLeaves.values());

        double nbLeavesExclusif, nbAncestorsInc, cur_ic;

        for (URI v : allNbAncestors.keySet()) {

            nbAncestorsInc = allNbAncestors.get(v).doubleValue();
            nbLeavesExclusif = allNbOfReachableLeaves.get(v).doubleValue();

            cur_ic = compute(nbLeavesExclusif, nbAncestorsInc, max_leaves);
            results.put(v, cur_ic);
        }
        return results;
    }

    /**
     *
     * @param nbLeaves
     * @param nbAncestors
     * @param maxLeaves
     * @return the IC considering the parameters.
     */
    public double compute(double nbLeaves, double nbAncestors, double maxLeaves) {

        double x = (nbLeaves / nbAncestors) / maxLeaves;

        return -MathSML.log(x, getLogBase());
    }

    @Override
    public Map<URI, Double> compute(IC_Conf_Topo conf, SM_Engine manager)
            throws SLIB_Ex_Critic {

        setLogBase(conf);

        Map<URI, Integer> allNbAncestors = manager.getAllNbAncestorsInc();
        Map<URI, Integer> allNbReachableLeaves = manager.getAllNbReachableLeaves();

        return compute(allNbReachableLeaves, allNbAncestors);
    }
}
