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
package slib.sml.sm.core.measures.graph.framework.dag;

import java.util.HashSet;
import java.util.Set;
import org.openrdf.model.URI;

import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.SetUtils;

/**
 * Tversky A: Features of similarity. Psychological Review 1977, 84:327-352.
 * Implementation of the contrast model in a set-based manner.
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class Sim_Framework_DAG_Set_Tversky_1977 extends Sim_Framework_DAG_Set_abstract {

    /**
     *
     */
    public static final String k_param_name = "k";
    private double k = 0.5;

    /**
     * Create a Tversky Contrast Model semantic similarity instance with k
     * parameter equals to 0.5
     */
    public Sim_Framework_DAG_Set_Tversky_1977() {
    }

    /**
     * Create a Tversky Contrast Model semantic similarity instance.
     *
     * @param k the value of the k parameter
     */
    public Sim_Framework_DAG_Set_Tversky_1977(double k) {
        this.k = k;
    }

    @Override
    public double compare(Set<URI> ancA, Set<URI> ancB, SMconf conf) throws SLIB_Exception {

        if (conf != null && conf.containsParam(k_param_name)) {
            k = conf.getParamAsDouble(k_param_name);
        }

        Set<URI> interSecAncestors = SetUtils.intersection(ancA, ancB);

        HashSet<URI> diff_a_b = new HashSet<URI>(ancA);
        diff_a_b.removeAll(ancB);

        HashSet<URI> diff_b_a = new HashSet<URI>(ancB);
        diff_b_a.removeAll(ancA);

        int c1_c2_diff_size = diff_a_b.size();
        int c2_c1_diff_size = diff_b_a.size();

        double den = (double) interSecAncestors.size() + k * c1_c2_diff_size + (1 - k) * c2_c1_diff_size;

        double tversky = (double) interSecAncestors.size() / den;


        return tversky;
    }

    /**
     * @return the value of the k parameter
     */
    public double getK() {
        return k;
    }

    /**
     * Setter of the k parameter value
     *
     * @param k the new value of k
     */
    public void setK(double k) {
        this.k = k;
    }

    @Override
    public Boolean isSymmetric() {
        return k == 0.5;
    }
}
