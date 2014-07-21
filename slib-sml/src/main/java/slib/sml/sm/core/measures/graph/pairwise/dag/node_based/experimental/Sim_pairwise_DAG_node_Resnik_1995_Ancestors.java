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
package slib.sml.sm.core.measures.graph.pairwise.dag.node_based.experimental;

import java.util.Set;
import org.openrdf.model.URI;

import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.measures.graph.pairwise.dag.node_based.Sim_DAG_node_abstract;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Exception;
import slib.utils.ex.SLIB_Ex_Warning;

/**
 * Resnik P: Using Information Content to Evaluate Semantic Similarity in a
 * Taxonomy. In Proceedings of the 14th International Joint Conference on
 * Artificial Intelligence IJCAI. 1995, 1:448-453.
 *
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 *
 */
public class Sim_pairwise_DAG_node_Resnik_1995_Ancestors extends Sim_DAG_node_abstract {

    
    @Override
    public double compare(URI a, URI b, SM_Engine c, SMconf conf) throws SLIB_Exception {

        double sim = 0;
        Set<URI> dcas = c.getLCAs(a, b);

        if (dcas.isEmpty()) {
            throw new SLIB_Ex_Warning("No disjoint ancestors detected for " + a + " " + b + ", similarity set to 0");
        }

        URI mica = null;
        double mica_ic = -Double.MAX_VALUE;

        for (URI dca : dcas) {

            if (c.getIC(conf.getICconf(), dca) > mica_ic) {
                mica_ic = c.getIC(conf.getICconf(), dca);
                mica = dca;
            }
        }

        for (URI anc : c.getAncestorsInc(mica)) {
            sim += c.getIC(conf.getICconf(), anc);
        }
        return sim;
    }

}
