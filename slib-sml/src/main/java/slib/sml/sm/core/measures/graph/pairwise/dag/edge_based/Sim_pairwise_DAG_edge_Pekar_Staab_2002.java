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
import org.openrdf.model.URI;

import slib.graph.model.graph.weight.GWS;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 *
 * Pekar V, Staab S: Taxonomy learning: factoring the structure of a taxonomy
 * into a semantic classification decision. In COLING ’02 Proceedings of the
 * 19th international conference on Computational linguistics. Association for
 * Computational Linguistics; 2002, 2:1–7.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class Sim_pairwise_DAG_edge_Pekar_Staab_2002 extends Sim_DAG_edge_abstract {

    @Override
    public double compare(URI a, URI b, SM_Engine c, SMconf conf) throws SLIB_Ex_Critic {

        GWS weightingScheme = c.getWeightingScheme(conf.getParamAsString("WEIGHTING_SCHEME"));
        URI msa = c.getMSA(a, b, weightingScheme);
        URI root = c.getRoot();

        Map<URI, Double> allSpMsa = c.getAllShortestPath(msa, weightingScheme);

        double sp_mrca_root = allSpMsa.get(root);
        double sp_a_mrca = allSpMsa.get(a);
        double sp_b_mrca = allSpMsa.get(b);

        return sim(sp_mrca_root, sp_a_mrca, sp_b_mrca);
    }

    /**
     * Compute the semantic similarity considering the given parameters. The
     * MRCA corresponds to the Most Recent Ancestor.
     *
     * @param sp_mrca_root shortest path from the MRCA to the root
     * @param sp_a_mrca shortest path from the A to the MRCA
     * @param sp_b_mrca shortest path from the B to the MRCA
     * @return the semantic similarity
     */
    public double sim(double sp_mrca_root, double sp_a_mrca, double sp_b_mrca) {

        double den = sp_mrca_root + sp_a_mrca + sp_b_mrca;

        double sim;

        if (den == 0) // root versus root
        {
            sim = 1;
        } else {
            sim = sp_mrca_root / den;
        }

        return sim;
    }

    @Override
    public Boolean isSymmetric() {
        return true;
    }
}
