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
package slib.sml.sm.core.measures.others.groupwise.indirect.experimental;

import java.util.Set;
import org.openrdf.model.URI;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.measures.Sim_Groupwise_Indirect;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.impl.MatrixDouble;

/**
 * ﻿
 * Frohlich H, Speer N, Poustka A, Beissbarth T: GOSim--an R-package for
 * computation of information theoretic GO similarities between terms and gene
 * products. BMC bioinformatics 2007, 8:166. Implementation as defined in
 * equation 7 page 3/8
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 *
 */
public class Sim_groupwise_AVERAGE_NORMALIZED_GOSIM extends Sim_Groupwise_Indirect {

   
    public double sim(double avgScore_sA_vs_sB, double avgScore_sA_vs_sA, double avgScore_sB_vs_sB) {

        double den = Math.sqrt(avgScore_sA_vs_sA * avgScore_sB_vs_sB);
        if (den == 0) {
            return 0;
        }

        double sim = avgScore_sA_vs_sB / den;
        return sim;
    }

    @Override
    public double compare(Set<URI> setA, Set<URI> setB, SM_Engine rc, SMconf groupwiseconf, SMconf paiwiseconf) throws SLIB_Ex_Critic {

        MatrixDouble<URI, URI> results_setA_B = rc.getMatrixScore(setA, setB, paiwiseconf);
        MatrixDouble<URI, URI> results_setA_A = rc.getMatrixScore(setA, setA, paiwiseconf);
        MatrixDouble<URI, URI> results_setB_B = rc.getMatrixScore(setB, setB, paiwiseconf);

        double avgScore_sA_vs_sB = results_setA_B.getAverage();
        double avgScore_sA_vs_sA = results_setA_A.getAverage();
        double avgScore_sB_vs_sB = results_setB_B.getAverage();

        return sim(avgScore_sA_vs_sB, avgScore_sA_vs_sA, avgScore_sB_vs_sB);

    }
}