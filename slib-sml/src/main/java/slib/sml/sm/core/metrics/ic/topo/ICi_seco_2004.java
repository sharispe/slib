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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import slib.sml.sm.core.metrics.ic.utils.IC_Conf_Topo;
import slib.sml.sm.core.metrics.utils.LogBasedMetric;
import slib.sml.sm.core.utils.MathSML;
import slib.sml.sm.core.engine.SM_Engine;
import slib.utils.ex.SLIB_Ex_Critic;

/**
 * Seco N, Veale T, Hayes J: An Intrinsic Information Content Metric for
 * Semantic Similarity in WordNet. In 16th European Conference on Artificial
 * Intelligence. IOS Press; 2004, 16:1-5.
 *
 * IC range : [0,1]
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class ICi_seco_2004 extends LogBasedMetric implements ICtopo {

    /**
     * Compute the Information contents of the vertices specified in the given
     * stack.
     *
     * @param allDescendantsInc a map containing the set of inclusive
     * descendants for all the vertices contained in the graph. The information
     * content will be computed for each vertices composing the stack
     * considering the number of vertices in the graph equaling the number of
     * vertices in the stack. Note that the number of descendant is considered
     * to be inclusive i.e. the count of descendants of a concepts x must also
     * count x.
     * @return a result stack storing the information for each concepts
     * specified in the result stack specified in parameter.
     * @throws SLIB_Ex_Critic
     */
    public Map<URI, Double> compute(Map<URI, Set<URI>> allDescendantsInc) throws SLIB_Ex_Critic {

        Map<URI, Double> results = new HashMap<URI, Double>();


        double x, cur_ic, nbDesc;

        double setSize = allDescendantsInc.size(); // i.e. number of concepts in the processed graph

        for (URI v : allDescendantsInc.keySet()) {

            nbDesc = allDescendantsInc.get(v).size();

            try {
                cur_ic = computeIC(nbDesc, setSize);
            } catch (SLIB_Ex_Critic e) {
                throw new SLIB_Ex_Critic("Error computing IC of concept " + v + "\n" + e.getMessage());
            }

            results.put(v, cur_ic);
        }

        return results;
    }

    @Override
    public Map<URI, Double> compute(IC_Conf_Topo conf, SM_Engine engine) throws SLIB_Ex_Critic {

        setLogBase(conf);

        // The formulation of Seco do not consider inclusive descendants but add + 1 to correct
        return compute(engine.getAllDescendantsInc());
    }

    /**
     * Compute the IC considering the given parameters.
     *
     * @param nbInclusiveDescendants the number of inclusive descendants to
     * consider.
     * @param nbConceptsOnto the number of concepts composing the ontology.
     * @return the IC.
     * @throws SLIB_Ex_Critic
     */
    public double computeIC(double nbInclusiveDescendants, double nbConceptsOnto) throws SLIB_Ex_Critic {

        /*
         * The formulation of Seco do not consider inclusive descendants but add + 1 to correct
         * the nominator and thus obtain log(0) for a leaf.
         * In our case the behaviour as inclusive descendants are considered
         */

        double x = MathSML.log(nbInclusiveDescendants, getLogBase()) / MathSML.log(nbConceptsOnto, getLogBase());
        double ic = 1. - x;

        if (Double.isNaN(ic) || Double.isInfinite(ic)) {
            throw new SLIB_Ex_Critic(
                    "Incoherency found in IC " + this.getClass() + "\n"
                    + "NB inclusive Descendants       " + nbInclusiveDescendants + "\n"
                    + "NB concepts onto       " + nbConceptsOnto + "\n"
                    + "Log base       " + getLogBase() + "\n"
                    + "Log nbDesc     " + MathSML.log(nbInclusiveDescendants, getLogBase()) + "\n"
                    + "Log Set size   " + MathSML.log(nbConceptsOnto, getLogBase()) + "\n"
                    + "IC is set to " + ic + "\n"
                    + "Number of Descendants: " + nbInclusiveDescendants + "\n"
                    + "SetSize: " + nbConceptsOnto + "\n"
                    + "");
        }
        return ic;
    }
}
