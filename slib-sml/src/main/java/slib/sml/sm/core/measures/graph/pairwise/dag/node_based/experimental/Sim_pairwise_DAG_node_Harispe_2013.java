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

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import org.openrdf.model.URI;
import slib.sml.sm.core.engine.SM_Engine;
import slib.sml.sm.core.measures.graph.pairwise.dag.node_based.Sim_DAG_node_abstract;
import slib.sml.sm.core.measures.graph.pairwise.dag.node_based.Sim_pairwise_DAG_node_Lin_1998;
import slib.sml.sm.core.metrics.ic.utils.ICconf;
import slib.sml.sm.core.utils.SMconf;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.ex.SLIB_Exception;

/**
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class Sim_pairwise_DAG_node_Harispe_2013 extends Sim_DAG_node_abstract {

    public static final String aggregation_lca = "aggregation_lca";
    public static final String measure_param = "measure";
    public static final String[] acceptedMeasures = {"Resnik", "Lin"};
    public static final String[] acceptedAggregations = {"Max", "Min", "Avg", "Agg"};

    @Override
    public double compare(URI a, URI b, SM_Engine c, SMconf conf) throws SLIB_Exception {

        if (!conf.containsParam(measure_param)) {
            throw new SLIB_Ex_Critic("Measure " + conf + " requires a parameter '" + measure_param + "' to be defined");
        }
        String underlyingMeasure = (String) conf.getParam(measure_param);

        if (!conf.containsParam(aggregation_lca)) {
            throw new SLIB_Ex_Critic("Measure " + conf + " requires a parameter '" + aggregation_lca + "' to be defined");
        }
        String aggregationLCAstrat = (String) conf.getParam(aggregation_lca);


        double ic_a = computeICpropagated(c, conf.getICconf(), a);
        double ic_b = computeICpropagated(c, conf.getICconf(), b);

        Set<URI> lca = c.getLCAs(a, b);

        double ic_lca = computeIcLCA(c, conf.getICconf(), lca, aggregationLCAstrat);

        // Compute the IC of the LCA layer considering the given aggregation strategy



        double sim;
        if (underlyingMeasure.equals("Resnik")) {

            sim = ic_lca;
        } else if (underlyingMeasure.equals("Lin")) {
            sim = Sim_pairwise_DAG_node_Lin_1998.sim(ic_a, ic_b, ic_lca);
        } else {
            throw new SLIB_Ex_Critic("Measure " + underlyingMeasure + " is not a valid argument for the parameter '" + measure_param + "' in " + conf + " pairwise measure configuration, accepted parameters are " + Arrays.toString(acceptedMeasures));
        }

        return sim;
    }

    private double computeICpropagated(SM_Engine engine, ICconf icConf, URI vertex) throws SLIB_Ex_Critic {
        Map<URI, Double> ics = engine.computeIC(icConf);
        double ic = 0;

        for (URI v : engine.getAncestorsInc(vertex)) {
            ic += ics.get(v);
        }
        return ic;
    }

    private double computeICpropagated(SM_Engine engine, ICconf icConf, Set<URI> vertices) throws SLIB_Ex_Critic {
        Map<URI, Double> ics = engine.computeIC(icConf);
        double ic = 0;

        for (URI v : engine.getAncestorsInc(vertices)) {
            ic += ics.get(v);
        }
        return ic;
    }

    private double computeIcLCA(SM_Engine c, ICconf conf, Set<URI> lca, String aggregation_lca_strategy) throws SLIB_Ex_Critic {

        double ic_lca = 0;

        if (aggregation_lca_strategy.equals("Max")) {
            double max = 0;
            for (URI v : lca) {
                double ic = computeICpropagated(c, conf, v);
                if (ic > max) {
                    max = ic;
                }
            }
            ic_lca = max;
        } else if (aggregation_lca_strategy.equals("Min")) {
            Double min = null;
            for (URI v : lca) {
                double ic = computeICpropagated(c, conf, v);
                if (min == null || ic < min) {
                    min = ic;
                }
            }
            ic_lca = min;
        } else if (aggregation_lca_strategy.equals("Avg")) {
            double avg = 0;
            for (URI v : lca) {
                avg += computeICpropagated(c, conf, v);
            }
            ic_lca = avg / (double) lca.size();
        } else if (aggregation_lca_strategy.equals("Agg")) {
            ic_lca = computeICpropagated(c, conf, lca);
        } else {
            throw new SLIB_Ex_Critic("Aggregation Strategy " + aggregation_lca_strategy + " is not a valid argument for the parameter '" + aggregation_lca + "', accepted parameters are " + Arrays.toString(acceptedAggregations));
        }
        return ic_lca;
    }

}
