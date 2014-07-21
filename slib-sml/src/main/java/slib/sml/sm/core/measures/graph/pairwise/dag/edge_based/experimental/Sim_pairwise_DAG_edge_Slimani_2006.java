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
package slib.sml.sm.core.measures.graph.pairwise.dag.edge_based.experimental;

import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;

import slib.sml.sm.core.measures.graph.pairwise.dag.edge_based.utils.SimDagEdgeUtils;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.measures.graph.pairwise.dag.edge_based.Sim_DAG_edge_abstract;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.SetUtils;

/**
 * Slimani T, Boutheina BY, Mellouli K: A New Similarity Measure based on
 * Edge Counting. In World academy of science, engineering and technology,.
 * 2006:34–38.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class Sim_pairwise_DAG_edge_Slimani_2006 extends Sim_DAG_edge_abstract {

    @Override
    public double compare(URI a, URI b, SM_Engine c, SMconf conf) throws SLIB_Exception {

        Set<URI> ancestors_A = c.getAncestorsInc(a);
        Set<URI> ancestors_B = c.getAncestorsInc(b);
        Map<URI, Integer> maxDepths = c.getMaxDepths();

        return sim(a, b, ancestors_A, ancestors_B, maxDepths);
    }

    /**
     *
     * @param cA
     * @param cB
     * @param ancestors_A
     * @param ancestors_B
     * @param maxDepths
     * @return the similarity
     * @throws SLIB_Exception
     */
    public double sim(
            URI cA,
            URI cB,
            Set<URI> ancestors_A,
            Set<URI> ancestors_B,
            Map<URI, Integer> maxDepths) throws SLIB_Exception {

        double sim = 0;

        boolean sameHierarchy = false;

        if (ancestors_A.contains(cB) || ancestors_B.contains(cA)) {
            sameHierarchy = true;
        }


        Set<URI> interSecAncestors = SetUtils.intersection(ancestors_A, ancestors_B);

        if (!interSecAncestors.isEmpty()) {


            URI msa = SimDagEdgeUtils.searchMSA(interSecAncestors, maxDepths);

            double d_mrca = maxDepths.get(msa);
            double d_a = maxDepths.get(cA);
            double d_b = maxDepths.get(cB);


            double pf = computePF(d_a, d_b, d_mrca, sameHierarchy);

            sim = (double) ((2 * d_mrca) / (d_a + d_b + 1)) * pf;
        }

        return sim;
    }

    /**
     * Penalization factor
     *
     * @param d_a
     * @param d_b
     * @param d_mrca
     * @param sameHierarchy
     * @return penalization factor
     */
    private double computePF(double d_a, double d_b, double d_mrca, boolean sameHierarchy) {

        double pf = 0;

        double lambda = 0;

        if (!sameHierarchy) {
            lambda = 1;
        }


        /*
         * <!> Modification of the original formula
         * Addition of +1 to the denominateur to obtain pf(a,b) == 1
         * if a,b are part of the same hierarchy.
         * the formula proposed in the paper to not respect the specification proposed
         */
        double gamma = (1 - lambda) * (Math.min(d_a, d_b) - d_mrca + 1);
        double alpha = lambda * (1 / (Math.abs(d_a - d_b) + 1));


        pf = gamma + alpha;

        return pf;
    }
}
