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
package slib.sml.sm.core.measures.graph.pairwise.dag.node_based;

import org.openrdf.model.URI;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Exception;

/**
 * Jiang J, Conrath D: Semantic Similarity Based on Corpus Statistics and
 * Lexical Taxonomy. In In International Conference Research on Computational
 * Linguistics (ROCLING X). 1997, cmp-lg/970:15.
 *
 * Adaptation of JC in order to normalized values between [0,1] ﻿based on :
 *
 * Applying Normalization discussed in 1. Seco N, Veale T, Hayes J: An Intrinsic
 * Information Content Metric for Semantic Similarity in WordNet. In 16th
 * European Conference on Artificial Intelligence. IOS Press; 2004, 16:1–5.
 *
 * Which is a reformulation of:
 *
 * Pesquita C, Faria D, Bastos H, et al.: Metrics for GO based protein semantic
 * similarity: a systematic evaluation. BMC bioinformatics 2008, 9 Suppl 5:S4.s
 *
 * The normalization makes sens only if IC of compared concepts are normalized
 * [0;1]
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 *
 */
public class Sim_pairwise_DAG_node_Jiang_Conrath_1997_Norm extends  Sim_DAG_node_abstract {

    @Override
    public double compare(URI a, URI b, SM_Engine c, SMconf conf) throws SLIB_Exception {

        double ic_a = c.getIC(conf.getICconf(), a);
        double ic_b = c.getIC(conf.getICconf(), b);
        double ic_MICA = c.getIC_MICA(conf.getICconf(), a, b);

        return sim(ic_a, ic_b, ic_MICA);
    }

    /**
     * Compute the semantic similarity considering the given parameters.
     *
     * @param ic_a the IC of the vertex A
     * @param ic_b the IC of the vertex B
     * @param ic_MICA the IC of the Most Informative Ancestor of the concept A
     * and B
     * @return the semantic similarity
     */
    public double sim(double ic_a, double ic_b, double ic_MICA) {

        double jc = 1. - (ic_a + ic_b - 2. * ic_MICA) / 2.;
        return jc;
    }

    @Override
    public Boolean isSymmetric() {
        return true;
    }
}
