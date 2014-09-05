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
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Exception;

/**
 * Kyogoku R, Fujimoto R, Ozaki T, Ohkawa T: A method for supporting retrieval
 * of articles on protein structure analysis considering users ’ intention. BMC
 * Bioinformatics 2011, 12:S42. p 2
 *
 * Basic because Kyogoku propose a method to estimate edge weight
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 *
 */
public class Sim_pairwise_DAG_edge_Kyogoku_basic_2011 extends Sim_DAG_edge_abstract {

    @Override
    public double compare(URI a, URI b, SM_Engine c, SMconf conf) throws SLIB_Exception {

        int maxDepth = c.getMaxDepth();

        GWS weightingScheme = c.getWeightingScheme(conf.getParamAsString("WEIGHTING_SCHEME"));
        double sp_AtoB = c.getShortestPath(a, b, weightingScheme);
        double sp_BtoA = c.getShortestPath(b, a, weightingScheme);

        return sim(maxDepth, sp_AtoB, sp_BtoA);
    }

    /**
     *
     * Compute the semantic similarity considering the given parameters.
     *
     * @param maxDepth Max depth the maximal depth of a class/concept in the
     * graph.
     * @param sp_AtoB Shortest path from A to B
     * @param sp_BtoA Shortest path from B to A
     *
     * @return the similarity considering the given parameters.
     */
    public double sim(double maxDepth, double sp_AtoB, double sp_BtoA) {

        double sim = maxDepth * 2 - (sp_AtoB + sp_BtoA);

        return sim;
    }

    
}
