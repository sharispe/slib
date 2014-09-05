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
package slib.graph.io.loader.utils.filter.graph.metrics;

import slib.graph.io.loader.utils.filter.graph.FilterGraph;
import slib.utils.ex.SLIB_Ex_Critic;
import slib.utils.i.Conf;

/**
 *
 * @author Sébastien Harispe <sebastien.harispe@gmail.com>
 */
public class FilterGraph_Metrics extends FilterGraph {

    boolean removeEmpty = true;
    String metric = null;
    double value;

    /**
     *
     * @param id
     */
    public FilterGraph_Metrics(String id) {
        super(id, FilterGraph_Metrics_cst.TYPE);
    }

    /**
     *
     * @param conf
     * @throws SLIB_Ex_Critic
     */
    public FilterGraph_Metrics(Conf conf) throws SLIB_Ex_Critic {
        super(conf);

        String removeEmptys = (String) conf.getParam(FilterGraph_Metrics_cst.REMOVE_EMPTY);
        metric = (String) conf.getParam(FilterGraph_Metrics_cst.METRIC);

        if (removeEmptys != null) {
            if (removeEmptys.equalsIgnoreCase("true")) {
                removeEmpty = true;
            } else if (removeEmptys.equalsIgnoreCase("false")) {
                removeEmpty = false;
            } else {
                throw new SLIB_Ex_Critic("Only value true or false are admitted for field " + FilterGraph_Metrics_cst.REMOVE_EMPTY + " in " + this.getId());
            }

        }


        String value_s = (String) conf.getParam(FilterGraph_Metrics_cst.VALUE);
        if (value_s == null) {
            throw new SLIB_Ex_Critic("Please specify a value (attribut " + FilterGraph_Metrics_cst.VALUE + " for Filter " + this.getId());
        } else {
            try {
                value = Double.parseDouble(value_s);
            } catch (NumberFormatException e) {
                throw new SLIB_Ex_Critic("Please specify a numeric value for attribute " + FilterGraph_Metrics_cst.VALUE + " in filter " + this.getId() + " current value = " + value_s);
            }
        }
    }

    @Override
    public String toString() {

        String out = super.toString();

        out += "\nRemove empty : " + removeEmpty;
        out += "\nmetric	   : " + metric;
        out += "\n";
        return out;
    }

    /**
     *
     * @return remove empty values.
     */
    public boolean isRemoveEmpty() {
        return removeEmpty;
    }

    /**
     *
     * @param removeEmpty
     */
    public void setRemoveEmpty(boolean removeEmpty) {
        this.removeEmpty = removeEmpty;
    }

    /**
     *
     * @return the metric associated to the filter.
     */
    public String getMetric() {
        return metric;
    }

    /**
     *
     * @param metric
     */
    public void setMetric(String metric) {
        this.metric = metric;
    }

    /**
     *
     * @return the value
     */
    public double getValue() {
        return value;
    }

    /**
     *
     * @param value
     */
    public void setValue(double value) {
        this.value = value;
    }
}
