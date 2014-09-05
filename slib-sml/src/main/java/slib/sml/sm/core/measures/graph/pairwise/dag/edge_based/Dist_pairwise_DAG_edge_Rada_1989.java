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
package slib.sml.sm.core.measures.graph.pairwise.dag.edge_based;

import org.openrdf.model.URI;
import slib.graph.model.graph.weight.GWS;
import slib.graph.model.impl.graph.weight.GWS_impl;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * ﻿Rada R, Mili H, Bicknell E, Blettner M: Development and application of a
 * metric on semantic nets. Ieee Transactions On Systems Man And Cybernetics
 * 1989, 19:17-30.
 *
 * In this implementation no restriction is made on the shortest path, i.e. if
 * the underlying data structure is a DAG the shortest path can in some case not
 * contain a common ancestor of the compared concepts.
 */
public class Dist_pairwise_DAG_edge_Rada_1989 extends Dist_DAG_edge_abstract {

    @Override
    public double compare(URI a, URI b, SM_Engine c, SMconf conf) throws SLIB_Ex_Critic {

        GWS weightingScheme;
        if (conf.getParamAsString("WEIGHTING_SCHEME") == null) {
            weightingScheme = new GWS_impl(1);
        } else {
            weightingScheme = c.getWeightingScheme(conf.getParamAsString("WEIGHTING_SCHEME"));
        }

        double sp = c.getShortestPath(a, b, weightingScheme);
        return sp;
    }
    
    @Override
    public Boolean isSymmetric() {
        // Depends on the symmetry of the weighting scheme
        return null;
    }
}
