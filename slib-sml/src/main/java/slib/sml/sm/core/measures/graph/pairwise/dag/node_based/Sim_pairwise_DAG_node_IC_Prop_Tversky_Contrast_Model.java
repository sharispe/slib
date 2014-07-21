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

import java.text.DecimalFormat;
import org.openrdf.model.URI;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;
import slib.utils.impl.SetUtils;

/**
 * IC formulation of Tversky Ratio Model
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 *
 */
public class Sim_pairwise_DAG_node_IC_Prop_Tversky_Contrast_Model extends Sim_DAG_node_abstract {

    public static final String gamma_param_name = "gamma";
    public static final String alpha_param_name = "alpha";
    public static final String beta_param_name = "beta";
    protected double gamma = 1.;
    protected double alpha = 1.;
    protected double beta = 1.;

    public Sim_pairwise_DAG_node_IC_Prop_Tversky_Contrast_Model() {
    }

    /**
     * Create a Tversky measure specifying gamma, alpha and beta parameters
     *
     * @param gamma importance of commonality
     * @param alpha importance of part of A not in B
     * @param beta importance of part of B not in A
     */
    public Sim_pairwise_DAG_node_IC_Prop_Tversky_Contrast_Model(double gamma, double alpha, double beta) {
        this.gamma = gamma;
        this.alpha = alpha;
        this.beta = beta;
    }

    @Override
    public double compare(URI a, URI b, SM_Engine c, SMconf conf) throws SLIB_Exception {

        if (conf != null && conf.containsParam(gamma_param_name)) {
            gamma = conf.getParamAsDouble(gamma_param_name);
        }
        if (conf != null && conf.containsParam(alpha_param_name)) {
            alpha = conf.getParamAsDouble(alpha_param_name);
        }

        if (conf != null && conf.containsParam(beta_param_name)) {
            beta = conf.getParamAsDouble(beta_param_name);
        }

        if (conf == null || conf.getICconf() == null) {
            throw new IllegalArgumentException("Measure " + this.getClass().getSimpleName() + " requires a configuration to be specified an IC to be specified");
        }

        ICconf icConf = conf.getICconf();

        double ic_a = 0;
        double ic_b = 0;
        double ic_common = 0;

        for (URI u : c.getAncestorsInc(a)) {
            ic_a += c.getIC(icConf, u);
        }
        for (URI u : c.getAncestorsInc(b)) {
            ic_b += c.getIC(icConf, u);
        }

        for (URI u : SetUtils.intersection(c.getAncestorsInc(a), c.getAncestorsInc(b))) {
            ic_common += c.getIC(icConf, u);
        }
        try {
            return sim(ic_a, ic_b, ic_common, gamma, alpha, beta);
        } catch (SLIB_Ex_Critic ex) {
            throw new SLIB_Ex_Critic(ex.getMessage() + "\n"
                    + "A = " + a + ", ancestors " + c.getAncestorsInc(a).size() + "\n"
                    + "B = " + b + ", ancestors " + c.getAncestorsInc(b).size() + "\n"
                    + "MICA , ancestors " + SetUtils.intersection(c.getAncestorsInc(a), c.getAncestorsInc(b)).size() + "\n");
        }
    }

    public static double sim(double ic_a, double ic_b, double ic_mica, double gamma, double alpha, double beta) throws SLIB_Ex_Critic {

        if (ic_mica > ic_a || ic_mica > ic_b) {

            // try to reduce the number of decimal 
            // errors are somethimes due to double precision  
            DecimalFormat df = new DecimalFormat("#0.######");
            ic_mica = Double.parseDouble(df.format(ic_mica));
            ic_a = Double.parseDouble(df.format(ic_a));
            ic_b = Double.parseDouble(df.format(ic_b));

            if (ic_mica > ic_a || ic_mica > ic_b) {

                throw new SLIB_Ex_Critic("Wrong parameters used with Tversky measure. "
                        + "IC MICA must be inferior to IC(a) and IC(b) \n"
                        + "IC A =  " + ic_a + "\n"
                        + "IC B =  " + ic_b + "\n"
                        + "IC MICA =  " + ic_mica + "\n");
            }
        }

        double j = 0.;

        if (ic_mica != 0) {
            j = gamma * (ic_mica) - alpha * (ic_a - ic_mica) - beta * (ic_b - ic_mica);
        }

        return j;
    }

    @Override
    public Boolean isSymmetric() {
        return alpha == beta;
    }
}
