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
package slib.sml.sm.core.metrics.ic.annot;

import java.util.HashMap;
import java.util.Map;
import org.openrdf.model.URI;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Corpus;
import slib.sml.sm.core.metrics.ic.utils.ProbOccurence;
import slib.sml.sm.core.metrics.utils.LogBasedMetric;
import slib.sml.sm.core.engine.SM_Engine;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 * Implementation of the original definition of Information Content (IC)
 * proposed by Resnik.
 *
 * <p>
 * <b>Reference</b>: Resnik, P. (1995). Using Information Content to Evaluate
 * Semantic Similarity in a Taxonomy. In Proceedings of the 14th International
 * Joint Conference on Artificial Intelligence IJCAI (Vol. 1, pp. 448–453).
 * </p>
 *
 * <p>
 * The IC of a concept u defined in a taxonomy is: -log(p(u)) with p(u) the
 * probability that the concept u occurs in a corpora or in an annotation
 * repository. The computation of the probability takes into account the
 * ordering of the concepts which is defined by the relation "rdfs:subClassOf".
 * In other words, if the concept u is subsumed by the concept v (i.e. u
 * rdfs:subClassOf v), any occurrence of the concept u is also an occurrence of
 * the concept v.
 * </p>
 * <p>
 * In order to avoid error i.e. -log(0) an occurrence is systematically
 * associated to all the leaf concepts specified in the taxonomy. A leaf concept
 * is a concept which does not subsumes any concept.
 * </p>
 *
 * @author Sébastien Harispe (sebastien.harispe@gmail.com)
 *
 */
public class IC_annot_resnik_1995 extends LogBasedMetric implements ICcorpus {

    /**
     * Computes the information content defined by Resnik considering the given
     * number of occurrences for each classes. For a concept u the IC is defined
     * by -log(p(u)). Note that no inference technique is used to propagate the
     * number of occurrences of a concept to its parent - This process is
     * expected to have already been performed and this method does not consider
     * the ordering of the concepts. In addition, the entry with the maximal
     * number of occurrences is considered to define the number of instances in
     * the repository. Thus the probability will be computed defining p(u) =
     * occurrences(u) / max_occurrences.
     *
     * @param nbOccurences the number of occurrences for each class. For each
     * class the number of occurrences must be greater than 0.
     * @return the IC of all URIs specified in the given map.
     * @throws SLIB_Ex_Critic
     */
    public Map<URI, Double> compute(Map<URI, Integer> nbOccurences) throws SLIB_Ex_Critic {

        Map<URI, Double> rtemp = ProbOccurence.compute(nbOccurences, 0);

        double curIc, pc;

        Map<URI, Double> results = new HashMap<URI, Double>();

        for (URI v : nbOccurences.keySet()) {

            pc = rtemp.get(v);

            //long nbOccMax = (long) nbOccurences.getMax()+1;
            //logger.debug(v+"\t"+nbOccurences.get(v)+"\t"+pc+"\t"+nbOccMax);
            curIc = -Math.log(pc);

            results.put(v, curIc);

        }
        return results;
    }

    @Override
    public Map<URI, Double> compute(IC_Conf_Corpus conf, SM_Engine manager) throws SLIB_Exception {

        setLogBase(conf);

        return compute(manager.getNbInstancesInferredPropFromCorpus(true));
    }
}
