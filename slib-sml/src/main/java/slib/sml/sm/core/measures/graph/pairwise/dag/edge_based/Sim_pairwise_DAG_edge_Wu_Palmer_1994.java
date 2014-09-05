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

import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;

import slib.graph.model.graph.weight.GWS;
import slib.sml.sm.core.measures.graph.pairwise.dag.edge_based.utils.SimDagEdgeUtils;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.SetUtils;

/**
 *
 * Wu Z, Palmer M: Verb semantics and lexical selection. In 32nd. Annual Meeting
 * of the Association for Computational Linguistics. 1994:133–138.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 *
 */
public class Sim_pairwise_DAG_edge_Wu_Palmer_1994 extends Sim_DAG_edge_abstract {

    @Override
    public double compare(URI a, URI b, SM_Engine c, SMconf conf) throws SLIB_Exception {

        GWS weightingScheme = c.getWeightingScheme(conf.getParamAsString("WEIGHTING_SCHEME"));

        Set<URI> ancestors_A = c.getAncestorsInc(a);
        Set<URI> ancestors_B = c.getAncestorsInc(b);
        Map<URI, Double> distMin_a = c.getAllShortestPath(a, weightingScheme);
        Map<URI, Double> distMin_b = c.getAllShortestPath(b, weightingScheme);

        Map<URI, Integer> maxDepths = c.getMaxDepths();

        return sim(a, b, ancestors_A, ancestors_B, distMin_a, distMin_b, maxDepths);
    }

    /**
     * compute the semantic similarity considering the given parameters.
     *
     * @param cA concept A
     * @param cB concept B
     * @param ancestors_A inclusive ancestors of A
     * @param ancestors_B inclusive ancestors of B
     * @param distMin_a shortest path from A
     * @param distMin_b shortest path from B
     * @param maxDepths maximal depth of all concepts
     * @return the semantic similarity
     * @throws SLIB_Exception
     */
    public double sim(
            URI cA,
            URI cB,
            Set<URI> ancestors_A,
            Set<URI> ancestors_B,
            Map<URI, Double> distMin_a,
            Map<URI, Double> distMin_b,
            Map<URI, Integer> maxDepths) throws SLIB_Exception {

        double sim = 0;

        Set<URI> interSecAncestors = SetUtils.intersection(ancestors_A, ancestors_B);

        if (!interSecAncestors.isEmpty()) {

            URI msa = SimDagEdgeUtils.searchMSA(interSecAncestors, maxDepths);

            int d_mrca = maxDepths.get(msa) + 1;
            double sp_a_mrca = distMin_a.get(msa);
            double sp_b_mrca = distMin_b.get(msa);

            sim = (double) (2 * d_mrca) / (sp_a_mrca + sp_b_mrca + 2 * d_mrca);
        } else {
            throw new SLIB_Ex_Critic("Error using Wu & Palmer measure. The compared concepts (" + cA + "," + cB + ") do not have a common ancestor... This is required to compute their similarity using this measure. The graph have to be connex, you can root the graph to obtain such a graph.");
        }

        return sim;
    }

    @Override
    public Boolean isSymmetric() {
        return true;
    }
}
